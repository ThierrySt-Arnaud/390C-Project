%Clear workspace
close all
clearvars
clc

fs = 48000;

Aweighting = weightingFilter('A-weighting',fs)

%visualize(Afilter)
AFilter = getFilter(Aweighting);

Asos = get(AFilter, 'SOSMatrix');

[Atransferhi,Atranferlo] = sos2tf(Asos);
