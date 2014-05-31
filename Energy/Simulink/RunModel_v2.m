%
% Variables can be specified in the comments of a m-file.
% By doing so, the plug-in can automatically create 
% ModelCenter variables when the m-file is imported. Make sure to comment
% out the ModelCenter variables in the INPUT CHANGES section otherwise they
% will replace the value that ModelCenter sent. Check that the .mat does
% not have these in either.
% 
% variable: solarx double input
% variable: windx double input
% variable: renew double output 
clear allsoal
clc
ResultFull = zeros(100000,39); % Result Data Matrix Intialization
%Change 1000000 if more than 100,000 DOE runs
load Building.mat %Load base model data (sun, wind traces + turbine power curve for the base turbine)
load solar_atl_2010.mat %Load base model data - Solar Radiation data Atlanta Airport 2010
% base model data can changed below:
%%%%%%%%%%%%%%%%%%%%% INPUT CHANGES %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Demand Model
Dswitch = 0; % set demand to constant(DC) if set to 0, or daily cyclic (D) if set to 1
DC = 20; % constant demand (kW)
D = [0 5 7 20 22 23.99; 5 10 25 25 10 5]; % cyclic demand on daily basis [t1, t2, t3, ....; kW1 kW2 kW3....]
%% Weather Data Tuning
solarx = 1;         % Mutlitplier for solar energy input from sun
windx = 1;          % same but for wind speed
%% Turbine Changes
baseT = 3.0;          % Base 3kW turbine rating (has to match data in .mat above)
TurbineDerate = 1; % Multiply base rating kW
TurbineCost = 1000.0*baseT; % $1000 per kW x 3 = Base Cost of Turbine
TurbineCount = 1.0; % number of Turbines in system
TurbineOpCost = 0.0; % Turbine Operation Cost (NOT USED - do we need?)
%% Solar Panel Change
PVderating = 0.9; % derate of power (would not change it)
PanelkWCost = 2500; % Cost of panel per kW
PVrating = 200; % Size of the panel in m^2
%% Generator - DELETED
%GenkW = 5; %Generator rating
%NumGen = 1; % Number fo Generator
%GenC = 1000; % Generator Cost $
%% Storage System Change
BattC = 500; % $ Cost of battery per kWh
cap = 5; % total capacity of the battery kWh
maxCap = 0.85; % maximum usable percent of the battery capacity, energy in is assumed wasted above this threshold
minCap = 0.15; % Minimum %Threshold of battery capacity --> Grid comes ON below this threshold
deltaCap = 0.02; % minCap + deltaCap is the threshold for the generator to come ON
startCap = 0.5; % Start of the simulation SOC (don't change it or keep it consistent between runs)
batteryEff = 0.98; % One way efficiency of battery
%% Energy Cost (ONLY GRID LEFT)
ElecC = 0.12; % Electricity Cost $/kWh
%GasCperKW = 0.05; % Generator $/kWh generated (need to get a good number and see if this is how we want to compute it)
%ElecS = 0.1; % % sale price back to grid
%BusEff = 1; %loss in puttng back to grid (1, becasue battery loss already occured and should not be)
%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%% RUN THE ENERGY SIMULATION%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
sim('SolarRoof');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% 1 RUN FILE
count2 = 1;
%%%%%%%%%%% POST-PROCESS & SAVE %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% INPUTS
ResultFull(count2,1) = solarx;
ResultFull(count2,2) = windx;
ResultFull(count2,3) = baseT*TurbineDerate; % Peak Rating kW for Turbine
ResultFull(count2,4) = TurbineCost;
ResultFull(count2,5) = TurbineCount;
ResultFull(count2,6) = TurbineOpCost;
ResultFull(count2,7) = PVderating;
ResultFull(count2,8) = PanelkWCost;
ResultFull(count2,9) = PVrating;
ResultFull(count2,10) = 0; %GenkW;
ResultFull(count2,11) = 0;%NumGen;
ResultFull(count2,12) = 0;%GenC;
ResultFull(count2,13) = BattC;
ResultFull(count2,14) = cap;
ResultFull(count2,15) = maxCap;
ResultFull(count2,16) = minCap;
ResultFull(count2,17) = deltaCap;
ResultFull(count2,18) = startCap;
ResultFull(count2,19) = batteryEff;
ResultFull(count2,20) = ElecC;
ResultFull(count2,21) = 0; %GasCperKW;
%% OUTPUTS
ResultFull(count2,22) = Total_Solar_E(end,1); % Solar energy available
%%ResultFull(count2,23) = Tot_Wind_E(end,1); % Wind energy available
%%ResultFull(count2,24) = Used_Grid_E(end,1); % Used energy from Grid
ResultFull(count2,24) = 0; %Used_Gen_E(end,1); % USed Generator Energy
%%ResultFull(count2,25) = -Demand_E(end,1); % Demanded Energy
%%ResultFull(count2,26) = (1-((Used_Grid_E(end,1))/abs(Demand_E(end,1))))*100; % Renewable % (OBJECTIVE)
%%ResultFull(count2,27) = 0; %Gas_cost(end,1); % Used gas cost
%%ResultFull(count2,28) = Elec_cost(end,1); % Used Electricity Cost
%%ResultFull(count2,29) = TurbineDerate*baseT; % Turbine Peak KW
%%ResultFull(count2,30) = Turbine_Cost(end,1); % Turbine Cost
%%ResultFull(count2,31) = Panel_Cost(end,1); % SOlar Panel Cost
%%ResultFull(count2,32) = 0; %Gen_Cost(end,1); % Generator Cost (only depenadnt on # of generator - NOT ON RATING!!! - may need to change that)
%%ResultFull(count2,33) = Batt_cost(end,1); % Battery Cost
%%ResultFull(count2,34) = Batt_cost(end,1)+ Turbine_Cost(end,1)+ Panel_Cost(end,1) + TurbineOpCost; % Total Investement
%%ResultFull(count2,35) = (abs(Demand_E(end,1))*ElecC)-(Elec_cost(end,1)); % SAVING ON ENERGY compare to Electric Only (!!!assuming no loss with grid!!!!)
%%ResultFull(count2,36) = ResultFull(count2,34)/ ResultFull(count2,35); % ROI TIME in years
%%ResultFull(count2,37) = 0; %Grid_Sales(end); % Revenue from selling ectricity back to grid
%%ResultFull(count2,38) = Cycles(end); % Deep Cycles #

%if (1500/Cycles(end)) > 10;
 %   ResultFull(count2,39) = 10; % Battery life (year)
%else
   % ResultFull(count2,39) = 1500/Cycles(end);
%end

% Here you can set the value of the variable for output that you'd want ModelCenter to
% read
renew = ResultFull(count2,26)
solarOnly = ResultFull(count2,22)
BatteryLife = ResultFull(count2,39) %years - approximative
%% save results - be sure to change name of .mat file below if you don't want to overwrite it
save Results2.mat
%%%%%%%% END %%%%%%%






