# HELM Editor #


This HELM editor is based on the original tool developed within Pfizer and supports the first version of [HELM](https://pistoiaalliance.atlassian.net/wiki/spaces/PUB/pages/13795362/HELM+Notation) only. (i.e. without the later extensions that support ambiguity)

We recommend that new users try the webeditor, as it has fewer dependencies and is being more actively developed.



## Getting started ##

Releases are available in the GitHub [release](https://github.com/PistoiaHELM/HELMEditor/releases) folder. 

A full release history and Java docs are available at 
[http://pistoiahelm.github.io/HELM_Editor_Release_Notes.html](http://pistoiahelm.github.io/HELM_Editor_Release_Notes.html)


#### To install ####

 1. Download HELMEditor-version.zip file to your computer and unzip.
 2. Run the jar!

  
**Note:**
    
1. JRE 1.7 or higher is required, and Java is set on the path;
2. You can add an optional command line argument "-helm $HELM__NOTATION". If provided, the HELM Editor will start with $HELM_NOTATION loaded.


#### Licenses and restrictions ####
HELM is an open source project released under the MIT license. However it has some dependencies on third party tools. Two of them have specific licence requirements. 


- HELM Editor requires yFiles for Java and the GraphML extension from yWorks. If you wish to use the compiled software directly there is no need to buy a licence. If you wish to develop the software for your own use, trial versions of such libraries can be downloaded from yWorks at www.yworks.com. You need to update the ant build file with regard to yfiles.dir and jar file names before compiling and running the project.

- HELM Editor requires MarvinBeans version 5.0 or higher from ChemAxon. A copy of the required library (MarvinBeans-5.0.jar) is included for your convenience. ChemAxon has graciously agreed to provide a royalty-free license for the utilization of MarvinBeans directly through the HELM editor and notation toolkit (specifically for operations pertaining to the chemical representation of HELM monomers and macromolecules.) Please contact ChemAxon to obtain this license. Utilization of the MarvinBeans library outside of the aforementioned scope and beyond a trial basis requires the procurement of a regular MarvinBeans license.


## Defects ##

Please let us know by logging in the issue list. Even better - send us a fix via a pull request!



##  Further information ##

See the HELM [wiki](https://pistoiaalliance.atlassian.net/wiki/spaces/PUB/pages/13795367/HELM+Editor) for additional documentation and user guides


## Dependencies ##

#### HELM Editor 1.4.1 ####

- Java 1.7
- MarvinBeans 5.0
- yFiles for Java 2.12
- HELMNotationToolkit 1.4.1


#### HELM Editor 1.4 ####

- Java 1.7
- MarvinBeans 5.0
- yFiles for Java 2.12
- HELMNotationToolkit 1.4

#### HELM Editor 1.3 ####

- Java 1.7
- MarvinBeans 5.0
- yFiles for Java 2.12
- HELMNotationToolkit 1.3

#### HELM Editor 1.2 ####

- Java 1.6
- MarvinBeans 5.0
- yFiles for Java 2.6 plus graphml
- HELMNotationToolkit 1.1

#### HELM Editor 1.1 ####

- Java 1.6
- MarvinBeans 5.0
- yFiles for Java 2.6 plus graphml
- HELMNotationToolkit 1.0.1


#### HELM Editor 1.0 ####

- Java 1.6
- MarvinBeans 5.0
- yFiles for Java 2.6 plus graphml
- HELMNotationToolkit 1.0





