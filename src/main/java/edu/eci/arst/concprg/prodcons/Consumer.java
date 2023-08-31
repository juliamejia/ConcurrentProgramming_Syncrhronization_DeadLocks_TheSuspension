/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.Queue;

/**
 * Esta clase representa un consumidor en el modelo productor-consumidor.
 * Extiende la clase Thread, lo que indica que se ejecutará como un hilo.
 * El consumidor consume elementos de una cola de enteros.
 *
 * @author hcadavid
 */
public class Consumer extends Thread {

    private Queue<Integer> queue; // Cola compartida entre productor y consumidor

    /**
     * Constructor que recibe la cola a ser compartida con el productor.
     *
     * @param queue La cola de enteros compartida
     */
    public Consumer(Queue<Integer> queue) {
        this.queue = queue;
    }

    /**
     * Método run que define el comportamiento del hilo consumidor.
     */
    @Override
    public void run() {
        while (true) {
            synchronized (queue) { // Bloquea el acceso a la cola
                while (queue.isEmpty()) { // Si la cola está vacía
                    try {
                        queue.wait(); // Espera hasta que haya elementos en la cola
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                int elem = queue.poll(); // Saca un elemento de la cola
                System.out.println("Consumer consumes " + elem); // Imprime el elemento consumido
            }
        }
    }
}
