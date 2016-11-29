window.onload = function() {
        cast.receiver.logger.setLevelValue(0);
        window.castReceiverManager = cast.receiver.CastReceiverManager.getInstance();
        console.log('Starting Receiver Manager');

        castReceiverManager.onReady = function(event) {
          console.log('Received Ready event: ' + JSON.stringify(event.data));
          window.castReceiverManager.setApplicationState("Application status is ready...");
        };

        castReceiverManager.onSenderConnected = function(event) {
          console.log('Received Sender Connected event: ' + event.data);
          console.log(window.castReceiverManager.getSender(event.data).userAgent);
        };

        castReceiverManager.onSenderDisconnected = function(event) {
          console.log('Received Sender Disconnected event: ' + event.data);
          if (window.castReceiverManager.getSenders().length == 0) {
	        window.close();
	      }
        };

        castReceiverManager.onSystemVolumeChanged = function(event) {
          console.log('Received System Volume Changed event: ' + event.data['level'] + ' ' +
              event.data['muted']);
        };

        window.messageBus = window.castReceiverManager.getCastMessageBus(
              'urn:x-cast:com.colortv.hello.channel');

        window.messageBus.onMessage = function(event) {
          console.log('Message [' + event.senderId + ']: ' + event.data);

          displayText(event.data);

          window.messageBus.send(event.senderId, "Receiver received: " + event.data);
        }

        window.castReceiverManager.start({statusText: "Application is starting"});
        console.log('Receiver Manager started');
      };

      function displayText(text) {
        console.log(text);
        document.getElementById("message").innerHTML=text;
        window.castReceiverManager.setApplicationState(text);
      };