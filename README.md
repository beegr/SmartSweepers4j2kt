# SmartSweepers4j
This is a port of the C++ Smart Sweepers from http://www.ai-junkie.com

It is a program to simulate the evolution of tanks which sweep mines. For this it uses a genetic algorithm to select the fittest sweepers from one generation.
The direction in which a sweeper is heading is processed using a neural network.

- With <kbd>f</kbd> you can toggle between visual mode and fast mode
- The red ones are the top x elite tanks from the previous generation which are not permuted by the genetic algorithm
- Green mines increase the fitness of a sweeper when they collect it
- An addition made by me are the black boxes which decrease the fitness of a sweeper when it is collected and thus the sweepers should avoid
- To alter the behavior and evolution you can change the settings in res/params.ini

![Image of Smartsweepers in visual mode](img/smartSweepersGui.png)
![Image of Smartsweepers in statistics mode](img/smartSweepersFast.png)


