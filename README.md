# Distributed-Computing-Auctioning-System
A distributed auctioning system able to handle multiple concurrent clients and actively replicate server nodes.

## How to set up 

- Requires JGroups to be installed on your system and to be in your system environment variables.

```bash
RunProject.bat  # Runs Server 
RunClient.bat   # Runs Client (can be run many times on different terminals) 
RunReplica.bat <optional_argument_amount_of_replicas_integer>

```

## Features

- Symmetric Encryption of Requests
- Auction Logic
- Access Control
- Cryptographic Authentication (5-stage challenge-response protocol, both parties verifying each other)
- Active Replication of Server Nodes and database synchronisation
- Fault tolerance and fault safety challenge-response protocol, both parties verifying each other) 



## Screenshots 

![Alt text](https://github.com/Paris778/Distributed-Computing-Auctioning-System/blob/main/scrnShots/Capture.JPG "Title")
![Alt text](https://github.com/Paris778/Distributed-Computing-Auctioning-System/blob/main/scrnShots/Capture2.JPG "Title")
![Alt text](https://github.com/Paris778/Distributed-Computing-Auctioning-System/blob/main/scrnShots/Capture3.JPG "Title")
![Alt text](https://github.com/Paris778/Distributed-Computing-Auctioning-System/blob/main/scrnShots/Capture4.JPG "Title")


## License
[MIT](https://choosealicense.com/licenses/mit/)
