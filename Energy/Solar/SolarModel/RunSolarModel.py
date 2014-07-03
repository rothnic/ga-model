__author__ = 'Nick'

import os

from dataIO import *
from SolarModel import *

path = os.path.dirname(os.path.realpath(__file__))

# Reads data in as rows of key=value pairs, and returns a python dict
data = readData(path + '\\data.in')

# This calls the runModel function with the data read in from data.in
run_model(data['panelRating'], data['panelRating'], data['panelRating'],
          data['panelRating'], data['panelRating'], data['panelRating'], data['panelRating'])

# Here we create a python dict as a placeholder for the output values
# This would be replaced once runModel returns an output dict
data = {}
data['solarSurfaceArea'] = 0.0
data['totalkWh'] = 0.0
data['solarCapitalCost'] = 0.0

# Here a new dict is written to data.out as key=value pairs
writeData(path + '\\data.out', data)