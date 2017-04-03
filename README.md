# Capstone-Project

Repository for final project for Udacity Nanodegree.

**Capstone Project Stage-01 and Stage-02**

## Overview 

Here is the fully functional app which is a part of *Android Developer Nanodegree Program from Udacity*.

*QuakeALert* is an app which displays the earthquakes that occured from the past day. It fetches data from the [USGS](https://www.usgs.gov/) website 

## Features

* Magnitude and name of the place where the earthquake has occured.
* Detail view of the place on the map where it has occured.
* Location includinng lattitude and longitde.
* How far the earthquake happened from you.
* *Share* the earthequake details.

### Earthquakes can be sorted according to the following
* Magnitude.
  * Minimum 4.5.
  * Minimum 2.5.
  * Minimum 1.0.
  * All eathquakes occured from past day.


### Notifications
Empowers the user with notifications when there is a earthquake :
* with magnitude greater than 7
* nearby location, country or worldwide.

## Preview
![](https://raw.githubusercontent.com/suryachintu/Capstone-Project/master/app/assets/sc_1.png)
![](https://raw.githubusercontent.com/suryachintu/Capstone-Project/master/app/assets/sc_2.png)
![](https://raw.githubusercontent.com/suryachintu/Capstone-Project/master/app/assets/sc_4.png)
![](https://raw.githubusercontent.com/suryachintu/Capstone-Project/master/app/assets/sc_8.png)
![](https://raw.githubusercontent.com/suryachintu/Capstone-Project/master/app/assets/sc_5.png)
![](https://raw.githubusercontent.com/suryachintu/Capstone-Project/master/app/assets/sc_9.png)

## Libraries used
* [ButterKnife](http://jakewharton.github.io/butterknife/)
* [OkHttp](http://square.github.io/okhttp/)

## Key Concepts
* Use of sync adapters.
* Use of SQLite and Content Providers.
* Support of larger screens (Tablets,Multi Pane ui).
* JSON Parsing.
* Unit Testing.
