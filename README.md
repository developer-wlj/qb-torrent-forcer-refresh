# qb-torrent-forcer-refresh

种子在下载状态时，默认每2分钟向Tracker服务器发起获取新peers请求

运行方式
1. qbRefresh (默认访问http://127.0.0.1:8080，刷新时间每2分钟)
2. qbRefresh `<url>` 如：`qbRefresh http://127.0.0.1:8080`
3. qbRefresh `<url> <refreshTime>` 如：`qbRefresh http://127.0.0.1:8080 5`