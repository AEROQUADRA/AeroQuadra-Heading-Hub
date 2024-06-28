
# AeroQuadra Heading Hub

AeroQuadra Heading Hub is an Android application designed to transform any Android device into an accurate compass that communicates with a Raspberry Pi (or any web server) via WebSocket. This application is ideal for robotic projects or any application requiring precise heading data.

## Features

- **Accurate Compass**: Uses the device's rotation vector sensor to provide real-time heading data.
- **WebSocket Communication**: Sends heading data to a specified server in real-time.
- **Flexible Usage**: Can be used with any web server, not limited to Raspberry Pi.
- **Permissions**: Requires location permissions for accessing the rotation vector sensor.

## Getting Started

### Prerequisites

- An Android device with a rotation vector sensor.
- A server to receive heading data (example Python server code provided).

### Installation

1. Clone the repository to your local machine.
2. Open the project in Android Studio.
3. Build and run the app on your Android device.

### Permissions

Ensure the app has the necessary permissions to access location and internet:

```xml
<uses-permission android:name="android.permission.BODY_SENSORS"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-feature android:name="android.hardware.sensor.compass" android:required="true" />
```

### Usage

1. **Start the Server**: Run the following Python script on your server to listen for incoming connections and print the received heading data.

   ```python
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
   ```

2. **Configure the App**:
   - Enter the IP address and port of your server in the app.
   - Press the "Connect" button to establish a connection.

3. **View Heading Data**:
   - The app will display the heading in degrees.
   - If connected to the server, the heading data will be sent in real-time.

### Screenshots

![App Screenshot](path/to/screenshot.png)

## License

This work is licensed under a Creative Commons Attribution-NonCommercial 4.0 International License. To view a copy of this license, visit [http://creativecommons.org/licenses/by-nc/4.0/](http://creativecommons.org/licenses/by-nc/4.0/).

### Commercial Use

For any commercial use of this software, you must obtain a commercial license from the original author. Please contact udarasampathx@gmail.com for details on obtaining a commercial license.

## Disclaimer

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES, OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT, OR OTHERWISE, ARISING FROM, OUT OF, OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Acknowledgements

- The Android Open Source Project
- [Your Name or Organization]
