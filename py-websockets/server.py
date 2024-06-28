import socket

# Create a socket object
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Define the IP address and port to listen on
# Listen on all available interfaces and port 12345
server_address = ('', 12345)

# Bind the socket to the server address
server_socket.bind(server_address)

# Listen for incoming connections
server_socket.listen(1)

print("Waiting for a connection...")

while True:
    connection, client_address = server_socket.accept()
    try:
        print("Connection from", client_address)

        while True:
            data = connection.recv(1024)
            if data:
                print("Received:", data.decode())
            else:
                break  # Exit inner loop if no more data received

    finally:
        connection.close()
