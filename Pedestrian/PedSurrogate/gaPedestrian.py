#from lib2to3 import __main__

__author__ = 'Nick'

import neurolab as nl
import numpy as np
import pylab as pl

def genPedestrians():
    # Create train samples
    x = np.linspace(-7, 7, 20)
    y = np.sin(x) * 0.5

    size = len(x)

    inp = x.reshape(size,1)
    tar = y.reshape(size,1)
    print tar
    # Create network with 2 layers and random initialized
    net = nl.net.newff([[-7, 7]],[5, 1])

    # Train network
    error = net.train(inp, tar, epochs=500, show=100, goal=0.02)

    # Simulate network
    out = net.sim(inp)

    # Plot result

    pl.subplot(211)
    pl.plot(error)
    pl.xlabel('Epoch number')
    pl.ylabel('error (default SSE)')

    x2 = np.linspace(-6.0,6.0,150)
    y2 = net.sim(x2.reshape(x2.size,1)).reshape(x2.size)

    y3 = out.reshape(size)

    pl.subplot(212)
    pl.plot(x2, y2, '-',x , y, '.', x, y3, 'p')
    pl.legend(['train target', 'net output'])
    pl.show()

    print tar

genPedestrians()