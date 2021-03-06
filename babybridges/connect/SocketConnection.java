package babybridges.connect;

import io.socket.client.IO;
import io.socket.emitter.Emitter;

import java.util.*;

import org.json.JSONObject;

// Wrapper for a socket.io socket connection for BRIDGES
public class SocketConnection {

	// This is the actual socket
	private io.socket.client.Socket socket;

	// Keep track of keypress listeners
	private List<KeypressListener> listeners = new ArrayList<KeypressListener>();

	// Register listeners to keypress events
	public void addListener(KeypressListener toAdd) {
		System.out.println("subscribing to keypress events..");
		listeners.add(toAdd);
	}

	// Given username and assignment number, set up a connection to the socket
	// server
	// TODO: this will need to be refactored to sit within BRIDGES, but for now
	// we pass credentials directly
	public void setupConnection(String user, String assignment) {
		try { // connect to the socket server

			IO.Options opts = new IO.Options();
			opts.transports = new String[] { "websocket" };

			// is the socket server running locally?
			// socket = IO.socket("http://localhost:3000", opts);

			// is the game server live?
			socket = IO.socket("https://bridges-games.herokuapp.com", opts);

			/*
			 * bind listeners to specific socket events
			 */
			socket.on(io.socket.client.Socket.EVENT_CONNECT,
					new Emitter.Listener() {
						// EVENT_CONNECT events are emitted by the server
						// whenever a socket connection is initiated and seen
						@Override
						public void call(Object... args) {
							/*
							 * Once socket connection is established, pass
							 * bridges credentials to set up channel access
							 */
							socket.emit(
									"credentials",
									"{\"user\":\""
											+ user
											+ "\",\"assignment\":\""
											+ assignment.substring(0,
													assignment.indexOf("."))
											+ "\"}");
							System.out
									.println("passing credentials to socket server...");
						}
					})
					.on("announcement", new Emitter.Listener() {
						// announcement events are emitted by the server
						// whenever another socket
						// subscribes to a channel on which this socket
						// connection is listening
						@Override
						public void call(Object... args) {
							JSONObject obj = (JSONObject) args[0];
							System.out.println("announcement: " + obj);
						}
					})
					.on("keydown", new Emitter.Listener() {
						// keydown events are emitted by the server whenever a
						// key is pressed
						// in a channel on which this socket connection is
						// listening
						@Override
						public void call(Object... args) {
							JSONObject obj = (JSONObject) args[0];
							// System.out.println(obj);

							// pass keypresses to all keypress listeners
							for (KeypressListener hl : listeners)
								hl.keypress(obj);
						}
					})
					.on("keyup", new Emitter.Listener() {
						// keyup events are emitted by the server whenever a key
						// is unpressed
						// in a channel on which this socket connection is
						// listening
						@Override
						public void call(Object... args) {
							JSONObject obj = (JSONObject) args[0];
							// System.out.println(args);

							// pass keypresses to all keypress listeners
							for (KeypressListener hl : listeners)
								hl.keypress(obj);
						}
					})
					.on(io.socket.client.Socket.EVENT_DISCONNECT,
							new Emitter.Listener() {
								// EVENT_DISCONNECT events are emitted by the
								// server if it crashes or terminates this
								// socket's connection
								@Override
								public void call(Object... args) {
									try {
										System.out.println(args);
										System.out
												.println("The server closed the connection.");
									} catch (Exception err) {
										System.out.println(err);
									}
								}
							});

			// Attempt to connect to the socket server
			socket.connect();

		} catch (Exception err) {
			System.out.println(err);
		}
	}

	// Send a dataframe string to the socket server
	// TODO: this currently emits a gamegrid:recv event. We may need to refactor
	// this to consider a variety of dataframe types
	public void sendData(String dataframe) {
		if (socket == null) {
			System.out.println("Cannot send data - socket is not connected.");
			return;
		}

		// the server will receive the grid dataframe and attempt to pass it to
		// any other sockets subscribed to the same channel
		socket.emit("gamegrid:recv", dataframe);
	}

	public void close() {
		socket.off();
		socket.disconnect();
	}
}
