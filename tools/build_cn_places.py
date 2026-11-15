#!/usr/bin/env python3
"""Generate app/src/main/assets/cn_places.json (offline Chinese place index).

Source: Aliyun DataV.GeoAtlas public administrative boundary GeoJSON
(https://geo.datav.aliyun.com/areas_v3/bound/). We only keep the name and the
administrative centre of each province / prefecture-level unit, which is all the
offline reverse lookup needs.

Usage:
    python tools/build_cn_places.py [output_path]

Default output: app/src/main/assets/cn_places.json (relative to repo root).
"""
import json
import os
import sys
import time
import urllib.request

BASE = "https://geo.datav.aliyun.com/areas_v3/bound/{}.json"
CACHE_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".geo-cache")

DEFAULT_OUT = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "app", "src", "main", "assets", "cn_places.json",
)

opener = urllib.request.build_opener()


def fetch(adcode):
    os.makedirs(CACHE_DIR, exist_ok=True)
    path = os.path.join(CACHE_DIR, f"{adcode}.json")
    if os.path.exists(path) and os.path.getsize(path) > 200:
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)

    url = BASE.format(adcode)
    for attempt in range(3):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "build-cn-places/1.0"})
            with opener.open(req, timeout=30) as r:
                data = r.read().decode("utf-8")
            with open(path, "w", encoding="utf-8") as f:
                f.write(data)
            return json.loads(data)
        except Exception as exc:  # noqa: BLE001 - network flakiness is expected
            print(f"  retry {attempt + 1} for {adcode}: {exc}", flush=True)
            time.sleep(2)
    return None


def center_of(props):
    c = props.get("center") or props.get("centroid")
    if isinstance(c, str):
        parts = c.split(",")
        if len(parts) != 2:
            return None
        try:
            return float(parts[0]), float(parts[1])
        except ValueError:
            return None
    if isinstance(c, list) and len(c) == 2:
        return float(c[0]), float(c[1])
    return None


def main():
    out_path = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_OUT

    root = fetch("100000_full")
    if not root:
        sys.exit("cannot download the national boundary file")

    places = []
    provinces = []
    for feature in root["features"]:
        props = feature["properties"]
        name = props.get("name")
        adcode = props.get("adcode")
        if not name or not adcode:
            continue
        provinces.append((name, adcode))
        centre = center_of(props)
        if centre:
            places.append({"n": name, "la": round(centre[1], 4), "lo": round(centre[0], 4), "p": ""})

    print(f"provinces: {len(provinces)}", flush=True)

    for province_name, adcode in provinces:
        doc = fetch(f"{adcode}_full")
        if not doc:
            print(f"  [skip] {province_name} ({adcode})", flush=True)
            continue
        count = 0
        for feature in doc.get("features", []):
            props = feature.get("properties", {})
            name = props.get("name")
            centre = center_of(props)
            if not name or not centre:
                continue
            places.append({
                "n": name,
                "la": round(centre[1], 4),
                "lo": round(centre[0], 4),
                "p": province_name,
            })
            count += 1
        print(f"  {province_name}: {count}", flush=True)

    seen = set()
    unique = []
    for place in places:
        key = (place["n"], place["la"], place["lo"])
        if key in seen:
            continue
        seen.add(key)
        unique.append(place)

    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(unique, f, ensure_ascii=False, separators=(",", ":"))

    print(f"TOTAL {len(unique)} places -> {out_path} "
          f"({os.path.getsize(out_path) / 1024:.1f} KB)", flush=True)


if __name__ == "__main__":
    main()