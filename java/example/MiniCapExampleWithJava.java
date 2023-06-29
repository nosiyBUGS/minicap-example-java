package example;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import gjTest.chatApp.Banner;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * @author hy
 * @create 2023/6/29 0029
 */
public class MiniCapExampleWithJava {
    ByteBuffer chunk = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
    static Log log = LogFactory.get();

    @Test
    public void example() throws IOException {

        SocketChannel stream = SocketChannel.open(new InetSocketAddress("localhost",1313));

        int readBannerBytes = 0;
        int bannerLength = 2;
        int readFrameBytes = 0;
        int frameBodyLength = 0;
        byte[] frameBody = new byte[0];
        Banner banner = new Banner();
        MyWebsocketServer server=new MyWebsocketServer(new InetSocketAddress("127.0.0.1",9002));
        server.start();
        System.out.println("server listen 9002");
        for (int read=0; ;read=stream.read(chunk)) {
            chunk.flip();
//                        log.info("chunk(length={})", chunk.limit());
            for (int cursor = 0, len = read; cursor < len;) {
                if (readBannerBytes < bannerLength) {
                    switch (readBannerBytes) {
                        case 0:
                            // version
                            banner.version = chunk.get();
                            break;
                        case 1:
                            // length
                            banner.length = bannerLength = chunk.get();
                            break;
//                                                case 2:
//                                                case 3:
//                                                case 4:
                        case 5:
                            // pid
                            banner.pid = chunk.getInt();
//                                                                +=
//                                                                (chunk.get() << ((readBannerBytes - 2) * 8)) >>> 0;
                            break;
//                                                case 6:
//                                                case 7:
//                                                case 8:
                        case 9:
                            // real width
                            banner.realWidth =chunk.getInt();
//                                                                +=
//                                                                (chunk.get() << ((readBannerBytes - 6) * 8)) >>> 0;
                            break;
//                                                case 10:
//                                                case 11:
//                                                case 12:
                        case 13:
                            // real height
                            banner.realHeight = chunk.getInt();//+=(chunk.get() << ((readBannerBytes - 10) * 8)) >>> 0;
                            break;
//                                                case 14:
//                                                case 15:
//                                                case 16:
                        case 17:
                            // virtual width
                            banner.virtualWidth = chunk.getInt();
//                                                                +=
//                                                                (chunk.get() << ((readBannerBytes - 14) * 8)) >>> 0;
                            break;
//                                                case 18:
//                                                case 19:
//                                                case 20:
                        case 21:
                            // virtual height
                            banner.virtualHeight =chunk.getInt();
//                                                                +=
//                                                                (chunk.get() << ((readBannerBytes - 18) * 8)) >>> 0;
                            break;
                        case 22:
                            // orientation
                            banner.orientation += chunk.get() * 90;
                            break;
                        case 23:
                            // quirks
                            banner.quirks = chunk.get();
                            break;
                    }

                    cursor += 1;
                    readBannerBytes += 1;

                    if (readBannerBytes == bannerLength) {
                        log.info("banner {}" , JSON.toJSONString(banner));
                    }
                }
                else if (readFrameBytes < 4) {

                    cursor += 1;
                    readFrameBytes += 1;
                    if(readFrameBytes==3){
                        frameBodyLength=chunk.getInt();
                        log.info("headerbyte{}(val={})", readFrameBytes, frameBodyLength);
                    }
                }
                else {
                    Assert.isTrue(frameBodyLength<100*10000);
                    if (len - cursor >= frameBodyLength) {
//                                                log.info("bodyfin(len={},cursor={})", frameBodyLength, cursor);
                        byte[] part=new byte[frameBodyLength];
                        chunk.get(part);
                        frameBody = ArrayUtil.addAll(frameBody, part);
                                               /* frameBody = Buffer.concat([
                                                        frameBody
                                                        , chunk.slice(cursor, cursor + frameBodyLength)*/
//            ])

                        // Sanity check for JPG header, only here for debugging purposes.
                        if (frameBody[0] != (byte)0xFF || frameBody[1] != (byte)0xD8) {
                            log.error(
                                    "Frame body does not start with JPG header", frameBody);
                            System.exit(1);
                        }
                        FileUtil.writeBytes(frameBody, new File("C:/frame.jpg"));

                        server.broadcast(frameBody);
                                                /*ws.send(frameBody, {
                                                        binary: true
            })*/

                        cursor += frameBodyLength;
                        frameBodyLength = readFrameBytes = 0;
                        frameBody = new byte[0];
                        new Scanner(System.in).nextLine();
                    }
                    else {
//                                                log.info("body(len={})", len - cursor);
                        byte[] part = new byte[chunk.limit()-chunk.position()];
                        chunk.get(part);
                        frameBody = ArrayUtil.addAll(frameBody, part);

                                                /*frameBody = Buffer.concat([
                                                        frameBody
                                                        , chunk.slice(cursor, len)*/
//            ])

                        frameBodyLength -= (len - cursor);
                        readFrameBytes += (len - cursor);
                        cursor = len;//break
                    }
                }
            }
            chunk.compact();
        }
    }
    public static void main(String[] args) throws IOException {
        new MiniCapExampleWithJava().example();
    }
}
