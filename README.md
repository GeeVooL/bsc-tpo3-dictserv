# Dictionary servers (TPO)

This is an exercise from the TPO classes on my university. The simplified
requirements are:
- Create a GUI client with that sends translation requests to the core
  server and waits for an answer on a separate ServerSocket
- Create a core server that accepts clients' requests and delegates them
  to specific dictionary servers.
- Create a dictionary server that accepts the requests from the core server,
  translates the given word and sends the translation result to the client's
  listening socket.
  
## Simplified setup flow
1. Run a CoreServer with an example configuration `configs/core/core.conf`.
2. Run DictServers. You can use example configs and dictionaries
   from the `configs/dict` directory. The dictionary servers automatically
   register themselves in the CoreServer specified in the configuration.
3. Run the Client app. Select the preferred language in the bottom left corner
   of the UI (the default one is English). Put the word you want to translate
   in the text field and confirm with the button in the bottom right corner.
   
