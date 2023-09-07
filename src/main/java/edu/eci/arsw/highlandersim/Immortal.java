package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;

    private int health;


    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());
    private boolean pause = false;
    private boolean stay = false;

    private AtomicInteger atomic;



    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        this.atomic = new AtomicInteger(health);
    }


    /**
     * Este método representa el comportamiento de un hilo de Immortal.
     * El hilo ejecuta una serie de acciones, incluyendo la lucha contra otro Immortal.
     */
    public void run() {
        while (!stay) {
            try {
                Immortal im;
                // Sincroniza el bloque de código con el objeto actual (this).
                synchronized (this) {
                    // Si la pausa está habilitada, el hilo se queda en espera.
                    if (pause) {
                        wait();
                    }
                }
                // Sincroniza el bloque de código con la lista de inmortales (immortalsPopulation).
                synchronized (immortalsPopulation) {
                    // Si la lista de inmortales está vacía, el hilo se queda en espera.
                    if (immortalsPopulation.isEmpty()) {
                        try {
                            immortalsPopulation.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // Obtiene el índice del Immortal actual en la lista de inmortales.
                int myIndex = immortalsPopulation.indexOf(this);

                // Genera un índice aleatorio para seleccionar otro Immortal.
                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                // Evita la lucha consigo mismo seleccionando el siguiente índice si es igual al actual.
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }

                // Obtiene el Immortal con el que luchará.
                im = immortalsPopulation.get(nextFighterIndex);

                // Llama al método de lucha (fight) con el Immortal seleccionado.
                this.fight(im);

                try {
                    // El hilo se duerme durante 1 milisegundo.
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Realiza una pelea entre dos inmortales.
     * @param i2 El Immortal con el que se va a pelear.
     */
    public void fight(Immortal i2) {
        // Obtiene los puntos de vida del Immortal i2 de manera atómica.
        int points = i2.getAtomic().get();

        // Verifica si i2 tiene puntos de vida positivos.
        if (points > 0) {
            // Sincroniza el bloque de código con el objeto updateCallback.
            synchronized (updateCallback) {
                // Sincroniza el bloque de código con el objeto i2.
                synchronized (i2) {
                    // Comprueba si los puntos de vida de i2 son iguales a 'points' y reduce los puntos de vida de i2 en 'defaultDamageValue' si es cierto.
                    if (i2.getAtomic().compareAndSet(points, points - defaultDamageValue)) {
                        // Marca a i2 como muerto.
                        i2.dead();
                        // Incrementa los puntos de vida del Immortal actual en 'defaultDamageValue'.
                        this.atomic.addAndGet(defaultDamageValue);
                        // Registra la información de la pelea en el callback de actualización.
                        updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                    }
                }
            }
        }
    }

    /**
     * Obtiene el objeto AtomicInteger utilizado para gestionar los puntos de vida de este Immortal.
     */
    public AtomicInteger getAtomic() {
        return this.atomic;
    }



    public synchronized void resumes() {
        pause = false;
        notifyAll();
    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    /**
     * Detiene el hilo actual, marcando al Immortal como "muerto".
     */
    public void dead() {
        // Detiene el hilo actual, indicando que el Immortal está "muerto".
        this.stop();
    }


}
