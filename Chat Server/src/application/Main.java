package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application {
	
	// 여러 클라이언트가 서버에 요청시 쓰레드를 효과적으로 관리하기 위해 threadPool 이용 (서버의 성능저하 방지)
	public static ExecutorService threadPool;
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	
	// 서버를 구동했을 때 클라이언트 요청을 기다리는 메소드 <클라이언트IP, 어느port 오픈>
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP, port));
		} catch (Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		
		// 클라이언트가 접속할 때까지 기다리는 쓰레드
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("[클라이언트 접속] "
								+ socket.getRemoteSocketAddress()
								+ ": "
								+ Thread.currentThread().getName());
					} catch(Exception e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool(); // 쓰레드풀 초기화
		threadPool.submit(thread);
	}
	
	// 서버의 작동을 중지하는 메소드
	public void stopServer() {
		try {
			// 현재 작동중인 소켓 모두 닫기
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			// 서버소켓 객체 닫기
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			// 쓰레드풀 종료
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// UI생성과 프로그램을 동작시키는 메소드
	// 디자인은 javaFX 이용
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));

		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("나눔고딕", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("시작하기");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";
		int port = 9876;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("시작하기")) {
				startServer(IP, port);
				Platform.runLater(() -> {
					String message = String.format("[서버 시작]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("종료하기");
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[서버 종료]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("시작하기");
				});
			}
		});
		
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("[ 채팅 서버 ]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	
	// 프로그램의 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
