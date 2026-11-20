package com.proyecto.slimefight.red;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RedUDP {

    public static class Mensaje {
        public final String texto;
        public final InetSocketAddress origen;

        Mensaje(String texto, InetSocketAddress origen) {
            this.texto = texto;
            this.origen = origen;
        }
    }

    private DatagramSocket socket;
    private Thread hilo;
    private volatile boolean activo = false;
    private final ConcurrentLinkedQueue<Mensaje> entrada = new ConcurrentLinkedQueue<>();

    public void abrir(int puerto) throws SocketException {
        socket = new DatagramSocket(null);
        socket.setReuseAddress(true);
        socket.setBroadcast(true);
        socket.bind(new InetSocketAddress(Math.max(puerto, 0)));
        activo = true;

        hilo = new Thread(() -> {
            byte[] buf = new byte[1024];
            while (activo) {
                try {
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    socket.receive(p);
                    String txt = new String(p.getData(), 0, p.getLength(), StandardCharsets.UTF_8);
                    entrada.add(new Mensaje(txt, new InetSocketAddress(p.getAddress(), p.getPort())));
                } catch (IOException e) {
                    if (!activo) break; // socket cerrado a proposito
                }
            }
        }, "RedUDP-receptor");
        hilo.setDaemon(true);
        hilo.start();
    }

    public Mensaje recibir() {
        return entrada.poll();
    }

    public void enviar(String texto, InetSocketAddress destino) {
        if (!activo || destino == null) return;
        try {
            byte[] datos = texto.getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(datos, datos.length, destino));
        } catch (IOException ignorado) {
            // UDP: si se pierde, se pierde
        }
    }

    public void enviarBroadcast(String texto, int puerto) {
        try {
            enviar(texto, new InetSocketAddress(InetAddress.getByName("255.255.255.255"), puerto));
        } catch (UnknownHostException ignorado) { }
    }

    public int getPuertoLocal() {
        return socket != null ? socket.getLocalPort() : -1;
    }

    public boolean estaAbierta() {
        return activo;
    }

    public void cerrar() {
        activo = false;
        if (socket != null) socket.close();
        entrada.clear();
    }

    public static String ipLocal() {
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces != null && ifaces.hasMoreElements()) {
                NetworkInterface ni = ifaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                Enumeration<InetAddress> dirs = ni.getInetAddresses();
                while (dirs.hasMoreElements()) {
                    InetAddress a = dirs.nextElement();
                    if (a instanceof Inet4Address && a.isSiteLocalAddress()) return a.getHostAddress();
                }
            }
        } catch (SocketException ignorado) { }
        return "127.0.0.1";
    }
}
