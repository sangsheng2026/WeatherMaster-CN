# 中国大陆网络优化版 · 修改说明

本仓库基于 [PranshulGG/WeatherMaster](https://github.com/PranshulGG/WeatherMaster)（v3.11.0，commit `11828aad`）修改，
目标是让 App 在**中国大陆、不使用代理**的网络环境下也能正常、快速地加载天气。

原版遵循 **GPL-3.0**，本修改版同样遵循 GPL-3.0，并在此说明全部修改内容。

---

## 一、问题现象

- 打开 App 后首页长时间没有天气，底部弹出「服务器未能及时响应」；
- 打开代理后一切正常；
- 首页标题一直显示坐标（如 `37.76943, 112.55863`），从不显示城市名。

## 二、根因（实测）

原版在请求天气**之前**，会串行执行一次「设备位置检查」，其中包含一次**反向地理编码**：

```
WeatherManager.invoke()
  └─ updateDeviceLocationPosition()          ← 必须先跑完，天气才发请求
       ├─ 取设备位置
       ├─ 判断是否移动超过 1km
       └─ 移动了 → GET https://nominatim.openstreetmap.org/reverse
                    ↑ 该域名在中国大陆被阻断（DNS 污染 + TCP 无响应）
                    OkHttp connectTimeout = 30s
```

实测数据（一加 12 / Android 16 / 无代理）：

| 项目 | 结果 |
|---|---|
| `api.open-meteo.com`（天气） | 200 / 1.79s |
| `air-quality-api.open-meteo.com` | 200 / 1.22s |
| `geocoding-api.open-meteo.com` | 200 / 3.20s |
| `secure.geonames.org` | 200 / 0.82s |
| `api.github.com` | 200 / 1.71s |
| **`nominatim.openstreetmap.org`** | **无响应（12s 超时 / SYN-SENT 持续 40s+）** |

因此：天气服务器本身没有问题，**是地名查询把天气请求堵了 30 秒**。
时间戳证据：App 进程 18:31:10 启动，天气数据 18:31:43 才写入数据库；而到 Open-Meteo 的连接一建立，1 秒内就返回成功。

此外还有三个次要问题：

1. **地名更新后首页不刷新**：城市名写入了数据库，但内存中的 `activeLocation` 快照没有同步，所以即使代理可用、地名查询成功，标题仍显示旧值。
2. **刷新指示器被辅助请求拖长**：`SourceDataRepository.getData()` 用 `coroutineScope` 等待天气 + 空气质量 + 预警全部结束，`setLoading(false)` 被推迟。
3. **首次启动需联网才能完成初始化**：`IntroScreenViewModel.saveDeviceLocation()` 先同步等待地名查询，再保存位置。

---

## 三、修改内容

| 文件 | 修改 | 目的 |
|---|---|---|
| `data/repository/WeatherContextRepository.kt` | 位置**先落库**（坐标 + 离线国家码），地名查询改为**后台协程**并加 `withTimeoutOrNull(5s)`；国家码查询限 2s；新增 `addressResolutionScope` | 地名查询不再阻塞天气 |
| `feature/intro/IntroScreenViewModel.kt` | 首次启动的地名查询限 5s，并加入离线城市名回退 | 首次启动不再可能卡 30 秒 |
| `core/managers/LocationManager.kt` | `writeLocations()` 中新位置同步进 `locationStore.activeLocation` | 修「地名更新但首页不刷新」 |
| `core/managers/WeatherManager.kt` | 天气结果一到就 `setLoading(false)`，不再等空气质量 / 预警 | 刷新指示器只跟天气走 |
| `data/provider/devicelocation/ChinaOfflinePlaceIndex.kt`（新增） | 离线城市名索引：509 个中国省市中心点，取最近点（≤100km），市级优先于省级 | 中国大陆无需联网即可显示「太原市」这类地名 |
| `app/src/main/assets/cn_places.json`（新增） | 紧凑的省市中心点数据（31.8 KB） | 同上 |
| `core/di/AppModule.kt` | 为 `WeatherContextRepository` 补齐新依赖 | 依赖注入 |
| `core/network/github/GithubApi.kt`、`feature/main/MainScreen.kt`、`feature/settings/about/AboutScreen.kt`、`feature/settings/sources/WeatherSourcesScreen.kt` | 更新检查与「源码 / 报告问题 / 新增数据源」链接从上游仓库改为本仓库 | 原版把更新检查的仓库地址写死成上游，导致装了这个修改版后仍被提示「有新版本」 |
| `WeatherManager.kt`、`BaseRepository.kt` | 刷新失败时输出一行 `Log.e` | 便于排查「首页空白且没有提示」这类静默失败 |

新增的离线索引完全在本地运行：不联网、不上传坐标、不引入新的第三方库。

## 四、修改后的实测效果

| 场景 | 原版 | 修改版 |
|---|---|---|
| 首次添加位置 → 首页出天气 | 32 秒 | **4 秒** |
| 冷启动 → 首页 | 受 30s 阻塞影响 | **2 秒** |
| 首页标题 | `37.81195, 112.59257` | **太原市**（离线解析） |
| 刷新指示器 | 等天气 + 空气质量 + 预警 | 天气返回即结束 |

天气来源、空气质量来源、预警来源均**未做任何改动**；其他国家和地区的使用行为不受影响（离线索引只在判定为中国大陆境内时启用）。

## 五、数据来源与许可

`app/src/main/assets/cn_places.json` 由公共行政区划边界数据（阿里云 DataV.GeoAtlas，`geo.datav.aliyun.com`）提取省/市行政中心点生成，
生成脚本见 `tools/build_cn_places.py`。该数据仅包含地名与中心点经纬度。

## 六、已知限制

- 离线索引到**地级市/区**粒度，不是街道级精度；距离最近城市中心超过 100km 时不使用（保留坐标名）。
- 地名固定为中文，不随界面语言变化。
- 台湾省的分级数据源缺失（生成时 404），该区域会退回到「省级」或坐标名。
- 原版的代理环境下行为不变，联网地名查询仍然保留（后台执行，成功后覆盖离线名）。

## 七、上游同步建议

以上修改都是独立的、可单独提交的补丁，建议按顺序分成 4 个 PR：
1. `fix: do not block weather refresh on reverse geocoding`
2. `fix: keep active location in sync`
3. `fix: stop refresh indicator at core weather result`
4. `feat: offline reverse geocoding for China`

---

## 八、3.11.0-cn2 增量修改（2026-11-16）

这一版只动界面与默认值，不涉及数据源实现。

| 文件 | 改动 | 原因 |
|---|---|---|
| `core/model/sources/Source.kt`（新增函数） | `defaultWeatherSourceFor(countryCode)`：中国返回 `Source.CHINA`，其他地区返回 `Source.OPEN_METEO` | 统一「默认数据源」的判断 |
| `feature/intro/IntroScreenViewModel.kt` | 保存设备位置时按境内判断写入 `source`（原版硬编码 `OPEN_METEO`） | 首次安装添加位置时，中国用户直接拿到国内数据源 |
| `feature/editlocation/EditLocationScreen.kt` | 编辑位置页的「天气数据源」在 `countryCode == CN` 且当前为 `OPEN_METEO` 时显示并保存为 `CHINA` | 老用户点开「笔」即可切到国内源 |
| `feature/settings/SettingsScreen.kt` | 「支持」条目替换为「项目仓库」（GitHub 图标 + 仓库链接）；移除 Discord 条目；删除捐赠对话框与相关代码 | 二次分发不应再有原作者赞助入口 |
| `feature/settings/background/BackgroundUpdatesScreen.kt` | 删除「更新」分组（后台更新开关）与「更新间隔」；保留后台任务信息 / Gadgetbridge / 电池优化 / 新版本提示 / 帮助 | 页面精简 |
| `feature/settings/background/WorkerInfoScreen.kt` | 文案中文化 | |
| `feature/settings/appearance/AppearanceScreen.kt` | 「Tablet layout」中文化 | |
| `feature/settings/notifications/NotificationsScreen.kt` | 「Alert notifications」中文化 | |
| `feature/apikeyconfig/ApiKeysConfigScreen.kt`、`feature/main/ui/MainScreenDialogs.kt` | 提示文案中文化 | |
| `feature/settings/about/AboutScreen.kt` | 版本卡片改名「WeatherMaster 大陆版」，副标题改为 `v<versionName> · Build <versionCode>`；新增「项目仓库」入口；修复版本号分隔符乱码 | |
| `feature/settings/about/terms/TermsConditionsScreen.kt` | 全文重写为中文，并按 GPL-3.0 二次分发说明权责；联系方式改为仓库 issue | 原版条款是英文且指向原作者邮箱 |
| `feature/settings/about/privacy/PrivacyPolicyScreen.kt` | 全文重写为中文，补充「中国境内城市名离线解析、不联网」的说明 | 同上 |
| `feature/shared/components/ChangelogContent.kt` | 更新日志改为本项目的中文变更记录；标题前缀 `#` 去掉 | 原版日志是上游英文 |
| `core/prefs/AppPrefs.kt` | 默认值调整：`appTheme = System`、`isDynamicTheme = true`、`isFroggyLayout = false` | 与维护者的日常设置一致 |
| `app/build.gradle.kts` | `versionName = 3.11.0-cn2`、`versionCode = 69` | |
| `res/values*/strings.xml` | `setting_background_updates_secondary` 描述去掉「更新间隔」 | 与精简后的页面一致 |

注意：`AppPrefs` 的默认值只对**全新安装**生效，已保存的偏好不会被覆盖。