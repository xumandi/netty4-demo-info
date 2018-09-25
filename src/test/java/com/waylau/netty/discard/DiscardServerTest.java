package com.waylau.netty.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class DiscardServerTest {
	
	private int port ;
	
	public DiscardServerTest(int port){
		this.port = port ;
	}
	
	public void run() throws Exception {
		//--接收进来的连接
		NioEventLoopGroup bossGroup = new NioEventLoopGroup();// (1)
		//--处理已经接收的连接
		NioEventLoopGroup workGroup = new NioEventLoopGroup();
		
		//--引导程序   bootstrap
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup,workGroup)
		           .channel(NioServerSocketChannel.class)
		           .childHandler(new ChannelInitializer<SocketChannel>() {
							@Override
							protected void initChannel(SocketChannel ch) throws Exception {
								ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){

									@Override
									public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
										ByteBuf in = (ByteBuf) msg;
									    try {
									        while (in.isReadable()) { // (1)
									            System.out.print((char) in.readByte());
									            System.out.flush();
									        }
									    } finally {
									        ReferenceCountUtil.release(msg); // (2)
									    }
									}

									@Override
									public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
											throws Exception {
										 // 当出现异常就关闭连接
								        cause.printStackTrace();
								        ctx.close();
									}
									
								});
							}
						})
		           .option(ChannelOption.SO_BACKLOG, 128)          // (5)
		           .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

		            // 绑定端口，开始接收进来的连接
		            ChannelFuture f = serverBootstrap.bind(port).sync(); // (7)

		            // 等待服务器  socket 关闭 。
		            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
		            f.channel().closeFuture().sync();
	}
	
	public static void main(String[] args) {
		try {
			new DiscardServerTest(9099).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
