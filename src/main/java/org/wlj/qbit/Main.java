package org.wlj.qbit;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.wlj.qbit.pojo.TorrentInfo;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author wlj
 */
public class Main {

    static Logger logger = Logger.getLogger("Main");

    public static void main(String[] args) {

        String qbServiceUrl="http://127.0.0.1:8080";
        int refreshTime=2;
        if (args.length==1) {
            qbServiceUrl= args[0];
        }
        if (args.length==2) {
            qbServiceUrl= args[0];
            if (0!=Integer.parseInt(args[1])){
                refreshTime= Integer.parseInt(args[1]);
            }
        }

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpRequest requestInfo = HttpRequest.newBuilder()
                .uri(URI.create(qbServiceUrl+"/api/v2/torrents/info"))
                .GET()
                .build();
        logger.info("qb强制汇报线程, 启动成功");
        while (true){
            try {
                TimeUnit.MINUTES.sleep(refreshTime);

                String respStr = client.send(requestInfo, HttpResponse.BodyHandlers.ofString()).body();
                Type listType = new TypeToken<List<TorrentInfo>>(){}.getType();
                List<TorrentInfo> torrentInfos= new Gson().fromJson(respStr, listType);
                List<TorrentInfo> listData = torrentInfos.stream().filter(torrentInfo -> "downloading".equals(torrentInfo.getState())||"stalledDL".equals(torrentInfo.getState()) || "forcedDL".equals(torrentInfo.getState())).collect(Collectors.toList());
                if (listData.isEmpty()) {
                    continue;
                }

                HttpRequest requestHashes = HttpRequest.newBuilder()
                        .uri(URI.create(qbServiceUrl+"/api/v2/torrents/reannounce"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"hashes\": \""+listData.stream().map(TorrentInfo::getInfohash_v1).collect(Collectors.joining("|"))+"\"}"))
                        .build();
                client.send(requestHashes, HttpResponse.BodyHandlers.ofString()).body();
                logger.info("已强制执行上报"+listData.size()+"个种子,Hashes为:"+listData.stream().map(TorrentInfo::getInfohash_v1).collect(Collectors.joining("|")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
