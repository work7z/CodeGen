package com.denote.client.test

class LocalGettingLocalDevInterfaceTesting {
    static void main(String[] args) {
//        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
//        for (NetworkInterface netint : Collections.list(nets))
//            displayInterfaceInformation(netint);
//        def myval = GlobalController.call(SystemAPILogicFunc.class, new APIRequest("net-cards", [
//        ]))
//        println myval
    }


    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        printf("Display name: %s\n", netint.getDisplayName());
        printf("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            printf("InetAddress: %s\n", inetAddress);
        }
        printf("\n");
    }
}
