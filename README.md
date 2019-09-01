# ChatApp
Chating Application using Java Socket Programming

LAN환경에서 동작할 수 있는 채팅 프로그램입니다.
JavaFx를 이용하여 GUI를 구성하였고, 서버와 클라이언트 모두 구현하였습니다.
서버에서는 쓰레드풀을 이용하여 클라이언트와의 소켓연결을 관리하여 overflow를 방지하였습니다.
클라이언트는 하나의 쓰레드로 서버에 접속 요청을 하고 bind하던 서버의 쓰레드가 accept하면 채팅을 이용할 수 있습니다.
