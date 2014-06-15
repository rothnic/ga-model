ga-model
===========

### Model for Georgia Aquarium

Consists of these major components:

* Energy Model
* Pedestrian Model
* Hydraulics Model
* Cost Model
* Uncertainties
* ModelCenter Integrating Model

---
### Getting Started
* Install http://www.sourcetreeapp.com/
* (important) Checkout this repository to:
```
C:\Workspace\ga-model
```

* (Optional) Might want to install [PyCharm](http://www.jetbrains.com/pycharm/download/) and possibly a light text editor like [Brackets](http://brackets.io/)
* Install Analysis Server and ModelCenter
* Set Analysis Server "Analyses Path" through web configuration to install directory
* Install Microsoft Excel
* Install Matlab with Simulink
* (Optional) Install AnyLogic

---
### ModelCenter Integrating Model
The ModelCenter model is used to run the system of systems optimization. The model file has been developed with a number of model assemblies, which allows each functional area to be worked on separate, and define a clear interface to the other model assemblies. If any assembly has been separately updated, they must be re-imported.


---
### Energy Model
Incorporates a model of the Georgia Aquarium power needs, along with options of renewable energy source

#### Simulink Model
Exposes input/output variables to ModelCenter via the RunModel.m header comments

---
### Pedestrian Model
This model was developed in AnyLogic, but will be converted into a surrogate model. This will allow the model to be used without running the software, since the University edition restricts users from integrating with other software via the API.
