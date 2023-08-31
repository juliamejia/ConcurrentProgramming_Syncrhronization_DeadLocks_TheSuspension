/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase es responsable de iniciar la producción y el consumo de elementos
 * en un modelo de productor-consumidor en un entorno concurrente.
 *
 * Utiliza una cola concurrente para compartir elementos entre el productor y el consumidor.
 * El productor crea productos durante un tiempo especificado y luego el consumidor los consume.
 *
 * @author
 */
public class StartProduction {

    private static int stock; // Variable para el límite de stock

    public static void main(String[] args) {
        stock = 100; // Límite de stock

        // Crear una cola de elementos con un tamaño máximo (stock)
        Queue<Integer> queue = new LinkedBlockingQueue<>(stock);

        // Crear y empezar el productor
        Producer producer = new Producer(queue, stock);
        producer.start();

        // Dejar que el productor cree productos durante 5 segundos (stock).
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(StartProduction.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Crear y empezar el consumidor
        new Consumer(queue).start();
    }
}
