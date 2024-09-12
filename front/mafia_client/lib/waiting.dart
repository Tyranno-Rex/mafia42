import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';

class WaitingRoom extends StatelessWidget {
  final int roomId;
  final String username;

  WaitingRoom({required this.roomId, required this.username});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter WebSocket Chat',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: WaitingRoomPage(roomId: roomId, username: username),
    );
  }
}

class WaitingRoomPage extends StatefulWidget {
  final int roomId;
  final String username;

  WaitingRoomPage({required this.roomId, required this.username});

  @override
  _WaitingRoomPageState createState() => _WaitingRoomPageState();
}

class _WaitingRoomPageState extends State<WaitingRoomPage> {
  late StompClient _stompClient;
  final String _serverUrl = 'http://192.168.45.40:8080/chatting';
  final String _sendEndpoint = '/app/message';
  final String _subscribeEndpoint = '/topic/message';

  final TextEditingController _messageController = TextEditingController();
  final List<Map<String, dynamic>> _messages = [];

  @override
  void initState() {
    super.initState();
    _initializeWebSocket();
  }

  void _initializeWebSocket() {
    _stompClient = StompClient(
      config: StompConfig.sockJS(
        url: _serverUrl,
        onConnect: _onConnect,
        onWebSocketError: (dynamic error) => print('WebSocket Error: $error'),
        onStompError: (dynamic frame) => print('Stomp Error: $frame'),
        onDisconnect: (dynamic frame) => print('Disconnected: $frame'),
      ),
    );
    _stompClient.activate();
  }

  void _sendMessage() {
    if (_messageController.text.isNotEmpty) {
      final message = {
        'content': _messageController.text,
        'uuid': widget.username,
        'roomId': widget.roomId
      };
      _stompClient.send(
        destination: _sendEndpoint,
        body: json.encode(message),
      );
      _messageController.clear();
    }
  }

  void _onConnect(StompFrame frame) {
    _stompClient.subscribe(
      destination: '$_subscribeEndpoint/${widget.roomId}',
      callback: (StompFrame frame) {
        if (frame.body != null) {
          final message = json.decode(frame.body!);
          setState(() {
            _messages.add(message);
          });
        }
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Chat Room ${widget.roomId}'),
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              itemCount: _messages.length,
              itemBuilder: (context, index) {
                final message = _messages[index];
                final isCurrentUser = message['uuid'] == widget.username;
                return _buildMessageBubble(message, isCurrentUser);
              },
            ),
          ),
          _buildMessageInput(),
        ],
      ),
    );
  }

  Widget _buildMessageBubble(Map<String, dynamic> message, bool isCurrentUser) {
    return Align(
      alignment: isCurrentUser ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: EdgeInsets.symmetric(vertical: 5, horizontal: 10),
        padding: EdgeInsets.symmetric(vertical: 10, horizontal: 15),
        decoration: BoxDecoration(
          color: isCurrentUser ? Colors.blue[100] : Colors.grey[300],
          borderRadius: BorderRadius.circular(20),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              message['uuid'],
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12),
            ),
            SizedBox(height: 5),
            Text(message['content']),
          ],
        ),
      ),
    );
  }

  Widget _buildMessageInput() {
    return Container(
      padding: EdgeInsets.all(8),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: _messageController,
              decoration: InputDecoration(
                hintText: 'Type a message...',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(25),
                ),
                contentPadding: EdgeInsets.symmetric(horizontal: 20, vertical: 10),
              ),
            ),
          ),
          SizedBox(width: 8),
          FloatingActionButton(
            onPressed: _sendMessage,
            child: Icon(Icons.send),
            mini: true,
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _stompClient.deactivate();
    _messageController.dispose();
    super.dispose();
  }
}