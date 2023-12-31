0#
# prepare soccer simulation
#

# setup soccer specific materials
# run "scripts/rcs-materials.rb"
if ($enableInternalMonitor)
    run "scripts/rcs-materials-textures.rb"
    
    material = sparkCreate('kerosin/Material2DTexture', $serverPath+'material/matGrass');
    material.setDiffuse(1.0,1.0,1.0,1.0)
    material.setAmbient(0.5,0.5,0.5,1.0)
    material.setDiffuseTexture('textures/rcs-naofield.png')
end

importBundle "soccer"

# soccer namespace
$soccerNameSpace = "Soccer"

# register a variable in the soccer namespace
def addSoccerVar(name, value)
   createVariable($soccerNameSpace, name, value)
end

# helper to get the value of a variable in the soccer namespace
def getSoccerVar(name)
  eval <<-EOS 
    #{$soccerNameSpace}.#{name} 
  EOS
end

# set a random seed (a seed of 0 means: use a random random seed)
randomServer = get($serverPath+'random')
if (randomServer != nil)
  randomServer.seed(0)
end

# the soccer field dimensions in meters
addSoccerVar('FieldLength', 10.0) # changed for RoboNewbie by nika, originally 30.0
addSoccerVar('FieldWidth', 7.0) # changed for RoboNewbie by nika, originally 20.0
addSoccerVar('FieldHeight', 40.0)
addSoccerVar('GoalWidth', 2.1)
addSoccerVar('GoalDepth', 0.6)
addSoccerVar('GoalHeight', 0.8)
addSoccerVar('PenaltyLength',0.1) # changed for RoboNewbie by nika, originally 1.8
addSoccerVar('PenaltyWidth',0.1) # changed for RoboNewbie by nika, originally 3.9
addSoccerVar('FreeKickDistance', 0)  #changed for RoboNewbie by  hdb, originally: 2.0
addSoccerVar('FreeKickMoveDist', 0)  #changed for RoboNewbie by  hdb, originally: 2.2
addSoccerVar('GoalKickDist', 0)      #changed for RoboNewbie by  hdb, originally: 1.0
addSoccerVar('BorderSize', 0.0) # prevent complaining about missing variable




# soccer game settings
addSoccerVar('AutomaticKickOff', false)
addSoccerVar('WaitBeforeKickOff', 2.0) 
     #changed for RoboNewbie according to former SomSpark by hdbNov13, originally: 30.0
addSoccerVar('CoinTossForKickOff', false)

addSoccerVar('AutomaticQuit', false)
addSoccerVar('ChangeSidesInSecondHalf', false)

# agent parameters
addSoccerVar('AgentRadius',  0.4)
addSoccerVar('MaxHeteroTypeCount', 3)  
addSoccerVar('MaxTotalHeteroCount', 9)

# ball parameters
addSoccerVar('BallRadius', 0.042)
addSoccerVar('BallMass',0.026)

# soccer rule parameters
addSoccerVar('RuleGoalPauseTime', 3.0)
addSoccerVar('RuleKickInPauseTime', 1.0)
addSoccerVar('RuleHalfTime', 60.0 * 60) #changed for RoboNewbie by nika, originally: 5.0 * 60
addSoccerVar('RuleDropBallTime', 0)   #changed for RoboNewbie by nika, originally: 15
addSoccerVar('SingleHalfTime', false)
addSoccerVar('UseOffside', false)
addSoccerVar('MaxTouchGroupSize', 2)



# charging foul parameters
addSoccerVar('UseCharging', true)
addSoccerVar('ChargingMinSpeed', 0.2)
addSoccerVar('ChargingMinBallDist', 0.2)
addSoccerVar('IllegalInterceptMinAngle', 70)

# auto ref parameters  FCP 2010
addSoccerVar('NotStandingMaxTime',30)       
addSoccerVar('GoalieNotStandingMaxTime',60)
addSoccerVar('GroundMaxTime',15)    
addSoccerVar('GoalieGroundMaxTime', 30)     
addSoccerVar('MaxPlayersInsideOwnArea',3)
addSoccerVar('MinOppDistance',0)      #changed for RoboNewbie by  hdb, originally: 0.8
addSoccerVar('Min2PlDistance',0)      #changed for RoboNewbie by  hdb, originally: 0.4
addSoccerVar('Min3PlDistance',0)      #changed for RoboNewbie by  hdb, originally: 1.0



# auto ref parameters for testing (not for use in competition...)
#addSoccerVar('NotStandingMaxTime',10)       
#addSoccerVar('GoalieNotStandingMaxTime',30)
#addSoccerVar('GroundMaxTime', 5)    
#addSoccerVar('GoalieGroundMaxTime', 5)     
#addSoccerVar('MaxPlayersInsideOwnArea',2)
#addSoccerVar('MinOppDistance',1.0)       
#addSoccerVar('Min2PlDistance',0.6)       
#addSoccerVar('Min3PlDistance',1.5)      

# recorders
addSoccerVar('BallRecorder',"Ball/geometry/recorder")
addSoccerVar('LeftGoalRecorder',"leftgoal/GoalBox/BoxCollider/recorder")
addSoccerVar('RightGoalRecorder',"rightgoal/GoalBox/BoxCollider/recorder")

# textures
createVariable('Nao', 'UseTexture', 'true')

scene = get($scenePath)
if (scene != nil)
  scene.importScene('rsg/agent/nao/soccer.rsg')
end

# setup the GameControlServer
gameControlServer = get($serverPath+'gamecontrol')
if (gameControlServer != nil)
  gameControlServer.initControlAspect('GameStateAspect')
  gameControlServer.initControlAspect('BallStateAspect')
  gameControlServer.initControlAspect('SoccerRuleAspect')
  obj = get('/sys/server/gamecontrol/GameStateAspect')
  if (obj != nil)
	  obj.setTime(0)
	  obj.setScores(0,0)
  end
end

# init monitorItems to transmit game state information
monitorServer = get($serverPath+'monitor')
if (monitorServer != nil)
  monitorServer.registerMonitorItem('GameStateItem')
  monitorServer.registerMonitorItem('SoccerRuleItem')
end

# install the TrainerCommandParser to parse commands received from a
# monitor client
sparkRegisterMonitorCmdParser 'TrainerCommandParser'

# Load parameters for heterogeneous Nao robots
run "naorobottypes.rb"
