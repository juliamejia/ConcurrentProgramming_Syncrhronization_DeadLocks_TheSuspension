
## Escuela Colombiana de Ingeniería
### Arquitecturas de Software – ARSW


#### Ejercicio – programación concurrente, condiciones de carrera y sincronización de hilos. EJERCICIO INDIVIDUAL O EN PAREJAS.

##### Parte I – Antes de terminar la clase.

Control de hilos con wait/notify. Productor/consumidor.

1. Revise el funcionamiento del programa y ejecútelo. Mientras esto ocurren, ejecute jVisualVM y revise el consumo de CPU del proceso correspondiente. A qué se debe este consumo?, cual es la clase responsable?  
	<img width="360" alt="image" src="https://github.com/juliamejia/ConcurrentProgramming_Syncrhronization_DeadLocks_TheSuspension/assets/98657146/a88c5901-d80f-482c-9932-e6507f730a00">

2. Haga los ajustes necesarios para que la solución use más eficientemente la CPU, teniendo en cuenta que -por ahora- la producción es lenta y el consumo es rápido. Verifique con JVisualVM que el consumo de CPU se reduzca.  
Se realizó la siguiente implementación en la Clase Cosumer, método run():  

	 ```java
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
	```

	Lo que nos muestra una reduccion del 12% al 0.1 %  
	<img width="340" alt="image" src="https://github.com/juliamejia/ConcurrentProgramming_Syncrhronization_DeadLocks_TheSuspension/assets/98657146/a89e06bd-1ed6-4e29-9b43-2914200c2226">  
3. Haga que ahora el productor produzca muy rápido, y el consumidor consuma lento. Teniendo en cuenta que el productor conoce un límite de Stock (cuantos elementos debería tener, a lo sumo en la cola), haga que dicho límite se respete. Revise el API de la colección usada como cola para ver cómo garantizar que dicho límite no se supere. Verifique que, al poner un límite pequeño para el 'stock', no haya consumo alto de CPU ni errores.  
Se Realizó la siguiente implementación en la clase Producerm, método run()  

	```java
	@Override
	    public void run() {
	        while (true) {
	
	            dataSeed = dataSeed + rand.nextInt(100);
	            System.out.println("Producer added " + dataSeed);
	            if(queue.size()<stockLimit) {
	                queue.add(dataSeed);
	            }else{
	                synchronized (queue){
	                    try {
	                        queue.wait();
	                    } catch (InterruptedException e) {
	                        throw new RuntimeException(e);
	                    }
	                }
	            }
	            try {
	                Thread.sleep(10);
	            } catch (InterruptedException ex) {
	                Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
	            }
	
	        }
	    }
	```

##### Parte II. – Avance para el jueves, antes de clase.

Sincronización y Dead-Locks.

![](http://files.explosm.net/comics/Matt/Bummed-forever.png)

1. Revise el programa “highlander-simulator”, dispuesto en el paquete edu.eci.arsw.highlandersim. Este es un juego en el que:

	* Se tienen N jugadores inmortales.
	* Cada jugador conoce a los N-1 jugador restantes.
	* Cada jugador, permanentemente, ataca a algún otro inmortal. El que primero ataca le resta M puntos de vida a su contrincante, y aumenta en esta misma cantidad sus propios puntos de vida.
	* El juego podría nunca tener un único ganador. Lo más probable es que al final sólo queden dos, peleando indefinidamente quitando y sumando puntos de vida.

2. Revise el código e identifique cómo se implemento la funcionalidad antes indicada. Dada la intención del juego, un invariante debería ser que la sumatoria de los puntos de vida de todos los jugadores siempre sea el mismo(claro está, en un instante de tiempo en el que no esté en proceso una operación de incremento/reducción de tiempo). Para este caso, para N jugadores, cual debería ser este valor?.  
Ya que cada jugador comienza con la misma cantidad de puntos y el único cambio en los puntos de vida ocurre cuando dos jugadores se enfrentan y uno gana puntos de vida mientras que el otro los pierde, por lo tanto:  
   $invariante = numeroJugadores*PuntosIniciales$.
   * En este caso tenemos que PuntosIniciales=100, entonces:  
     $invariante = numeroJugadores*100$

3. Ejecute la aplicación y verifique cómo funcionan las opción ‘pause and check’. Se cumple el invariante?.
   <img width="281" alt="image" src="https://github.com/juliamejia/ConcurrentProgramming_Syncrhronization_DeadLocks_TheSuspension/assets/98657146/5d845662-9e40-4a7b-8649-f5efd5dffee3">  
No se cumple ya que hay 3 jugadores e inician con 100 puntos, por lo tanto(segun lo desarrollado en el punto anterior):  
$Health sum = 300$  
Ademas de eso pudimos notar que este valor cambia constantemente  
4. Una primera hipótesis para que se presente la condición de carrera para dicha función (pause and check), es que el programa consulta la lista cuyos valores va a imprimir, a la vez que otros hilos modifican sus valores. Para corregir esto, haga lo que sea necesario para que efectivamente, antes de imprimir los resultados actuales, se pausen todos los demás hilos. Adicionalmente, implemente la opción ‘resume’.  
Se realizó esta implementación en la clase Inmortal, metodo run() para poder realizar la suma cuando se paren los hilos:

```java
if(pause){
        synchronized (im) {
            im.wait();
            pause = false;
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
```

5. Verifique nuevamente el funcionamiento (haga clic muchas veces en el botón). Se cumple o no el invariante?.  
   Aun no se cumple la invariante, ya que sigue enviando valores totalmente diferentes en cada "pause and check"  

6. Identifique posibles regiones críticas en lo que respecta a la pelea de los inmortales. Implemente una estrategia de bloqueo que evite las condiciones de carrera. Recuerde que si usted requiere usar dos o más ‘locks’ simultáneamente, puede usar bloques sincronizados anidados:

	```java
	synchronized(locka){
		synchronized(lockb){
			…
		}
	}
	```
 
	Se modificó el metodo fight en la clase Immortal

	```java
	public void fight(Immortal i2) {
	
	        int points = i2.getAtomic().get();
	
	        if (points > 0) {
	            synchronized (this) {
	                synchronized (i2) {
	                    if (i2.getAtomic().compareAndSet(points, points - defaultDamageValue)) {
	                        i2.dead();
	                        this.atomic.addAndGet(defaultDamageValue);
	                        updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
	                    }
	                }
	
	            }
	        }
	    }
	```

7. Tras implementar su estrategia, ponga a correr su programa, y ponga atención a si éste se llega a detener. Si es así, use los programas jps y jstack para identificar por qué el programa se detuvo.
bloqueo del programa  
	<img width="602" alt="image" src="https://github.com/juliamejia/ConcurrentProgramming_Syncrhronization_DeadLocks_TheSuspension/assets/98657146/e156cf7b-664f-486d-8f3d-e20d87f75f09">
Ejecutamos el comando jps
	<img width="932" alt="image" src="https://github.com/juliamejia/ConcurrentProgramming_Syncrhronization_DeadLocks_TheSuspension/assets/98657146/10046f5b-69f0-49c8-a701-f66a829daa72">

	Ejecutamos el comando Jstack

	<img width="542" alt="image" src="https://github.com/juliamejia/ConcurrentProgramming_Syncrhronization_DeadLocks_TheSuspension/assets/98657146/73f2e38a-7371-423c-9656-d0652eb1cf67">  

8. Plantee una estrategia para corregir el problema antes identificado (puede revisar de nuevo las páginas 206 y 207 de _Java Concurrency in Practice_).  
Se implementaron y modificaron los siguientes metodos en la clase Immortal:  

	```java
	private AtomicInteger atomic;
	```

	```java
	public void fight(Immortal i2) {
	
	    int points = i2.getAtomic().get();
	
	    if (points > 0) {
	        synchronized (updateCallback) {
	            synchronized (i2) {
	                if (i2.getAtomic().compareAndSet(points, points - defaultDamageValue)) {
	                    i2.dead();
	                    this.atomic.addAndGet(defaultDamageValue);
	                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
	                }
	            }
	
	        }
	    }
	}
	```

	```java
	public AtomicInteger getAtomic() {
	    return this.atomic;
	}
	public void dead() {
	    this.stop();
	}
	```

Vemos que ya se cumple el invariante  
	<img width="351" alt="image" src="https://github.com/juliamejia/ConcurrentProgramming_Syncrhronization_DeadLocks_TheSuspension/assets/98657146/bd1445ee-ecc5-4750-8868-953a5bbb180c">  

9. Una vez corregido el problema, rectifique que el programa siga funcionando de manera consistente cuando se ejecutan 100, 1000 o 10000 inmortales. Si en estos casos grandes se empieza a incumplir de nuevo el invariante, debe analizar lo realizado en el paso 4.
* 100  
  <img width="454" alt="image" src="https://github.com/juliamejia/ConcurrentProgramming_Syncrhronization_DeadLocks_TheSuspension/assets/98657146/58039915-0e21-458e-9a54-2d6870c6825b">
* 1000  
  <img width="641" alt="image" src="https://github.com/juliamejia/ConcurrentProgramming_Syncrhronization_DeadLocks_TheSuspension/assets/98657146/8a8543a8-13ff-40ad-bcf7-25a9b21a22b2">
* 10000  
  <img width="606" alt="image" src="https://github.com/juliamejia/ConcurrentProgramming_Syncrhronization_DeadLocks_TheSuspension/assets/98657146/26d285a1-d06d-4cc9-9e20-7da1a7d6f140">  
10. Un elemento molesto para la simulación es que en cierto punto de la misma hay pocos 'inmortales' vivos realizando peleas fallidas con 'inmortales' ya muertos. Es necesario ir suprimiendo los inmortales muertos de la simulación a medida que van muriendo. Para esto:
	* Analizando el esquema de funcionamiento de la simulación, esto podría crear una condición de carrera? Implemente la funcionalidad, ejecute la simulación y observe qué problema se presenta cuando hay muchos 'inmortales' en la misma. Escriba sus conclusiones al respecto en el archivo RESPUESTAS.txt.
   Respuesta adjuntada en el documento  
	* Corrija el problema anterior __SIN hacer uso de sincronización__, pues volver secuencial el acceso a la lista compartida de inmortales haría extremadamente lenta la simulación.  
 Se modificó el metodo run() en la clase Immortal de la siguiente manera:

	```java
	public void run() {
	    while (!stay) {
	        try{
	            Immortal im;
	            synchronized(this){
	                if (pause){
	                    wait();
	                }
	            }
	            synchronized(immortalsPopulation) {
	                if(immortalsPopulation.isEmpty()) {
	                    try {
	                        immortalsPopulation.wait();
	                    }catch (InterruptedException e) {
	                        e.printStackTrace();
	                    }
	                }
	            }
	            int myIndex = immortalsPopulation.indexOf(this);
	
	            int nextFighterIndex = r.nextInt(immortalsPopulation.size());
	            if (nextFighterIndex == myIndex) {
	                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
	            }
	
	            im = immortalsPopulation.get(nextFighterIndex);
	
	            this.fight(im);
	
	            try {
	                Thread.sleep(1);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }catch(InterruptedException e) {e.printStackTrace();}
	    }
	}
	```

11. Para finalizar, implemente la opción STOP.  
    Se implementa la funcionalidad para el boton "STOP" en la clase ControlFrame:

	```java
	    btnStop.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent event) {
	        for(Immortal i : immortals) {
	            i.dead();
	        }
	
	        JOptionPane.showMessageDialog(null, "La simulacion ha sido detenida!");
	        System.exit(0);
	
	    }
	});
	```

<!--
### Criterios de evaluación

1. Parte I.
	* Funcional: La simulación de producción/consumidor se ejecuta eficientemente (sin esperas activas).

2. Parte II. (Retomando el laboratorio 1)
	* Se modificó el ejercicio anterior para que los hilos llevaran conjuntamente (compartido) el número de ocurrencias encontradas, y se finalizaran y retornaran el valor en cuanto dicho número de ocurrencias fuera el esperado.
	* Se garantiza que no se den condiciones de carrera modificando el acceso concurrente al valor compartido (número de ocurrencias).


2. Parte III.
	* Diseño:
		- Coordinación de hilos:
			* Para pausar la pelea, se debe lograr que el hilo principal induzca a los otros a que se suspendan a sí mismos. Se debe también tener en cuenta que sólo se debe mostrar la sumatoria de los puntos de vida cuando se asegure que todos los hilos han sido suspendidos.
			* Si para lo anterior se recorre a todo el conjunto de hilos para ver su estado, se evalúa como R, por ser muy ineficiente.
			* Si para lo anterior los hilos manipulan un contador concurrentemente, pero lo hacen sin tener en cuenta que el incremento de un contador no es una operación atómica -es decir, que puede causar una condición de carrera- , se evalúa como R. En este caso se debería sincronizar el acceso, o usar tipos atómicos como AtomicInteger).

		- Consistencia ante la concurrencia
			* Para garantizar la consistencia en la pelea entre dos inmortales, se debe sincronizar el acceso a cualquier otra pelea que involucre a uno, al otro, o a los dos simultáneamente:
			* En los bloques anidados de sincronización requeridos para lo anterior, se debe garantizar que si los mismos locks son usados en dos peleas simultánemante, éstos será usados en el mismo orden para evitar deadlocks.
			* En caso de sincronizar el acceso a la pelea con un LOCK común, se evaluará como M, pues esto hace secuencial todas las peleas.
			* La lista de inmortales debe reducirse en la medida que éstos mueran, pero esta operación debe realizarse SIN sincronización, sino haciendo uso de una colección concurrente (no bloqueante).

	

	* Funcionalidad:
		* Se cumple con el invariante al usar la aplicación con 10, 100 o 1000 hilos.
		* La aplicación puede reanudar y finalizar(stop) su ejecución.
		
		-->

<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a><br />Este contenido hace parte del curso Arquitecturas de Software del programa de Ingeniería de Sistemas de la Escuela Colombiana de Ingeniería, y está licenciado como <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
