#! /usr/bin/env python3

import args
import server, client

if __name__ == '__main__':
    (listen_port, remote_host, remote_port, files) = args.check_args()

    # user may pass listen port 0
    if listen_port != None:
        server.run(listen_port, files)
    else:
        client.run(remote_host, remote_port, files)
