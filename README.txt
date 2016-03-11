3/11/2016
*added button to game screen that is helping me learn about how to add a pause and mute button to game screen - so basically not functional yet but present
*removed permission for wake lock - crashed app when running main game loop until i commented out all the wake lock code - my thoughts are wake locks are not needed for games
*removed permission for access network state - when clicking “online top 100” button in highscore, it will crash and complain about this mission permission. also crashed when i tried to save score in game until i commented out the check isOnline in main.java. now it only saves locally. commented out the submit local high score online button
*gonna work on implementing double jump


3/6/2016:
*Changed HighScoreForm’s perfomPostCall function to use AsyncTask to fix “networkonmainthread” error
*changed main.java to extend BaseGameActivity to use google api client for leaderboards
*implemented leaderboard for best run score

2/19/2016a:
*Added baseGameUtils as recommended from Google play games services website
*added some string resources
*changed name of game under icon and in app manager
*minor fixes with deprecated methods
*cleaned up some layout xml
*starting to add google play games stuff

2/19/2016:
*updated app name to RunnersHighEnhanced
*updated info about RunnersHighEnhanced
*removed highscore_background.jpg from created folder drawable_nodpi - was causing error
*min sdk version is now API 9

2/17/2016  - Updates added after fork by user nurseymybush:
*Import into Gradle Build System plugin 1.5.0
*Change Target to API 23, Min SDK to 8
*Android Studio 1.5.1
*Fixed AndroidManifest.xml to current standards
*CLASS HighScoreActivity - replaced the deprecated or.apache.http with httpurlconnection in functions onCreate and showOnlineScore
*CLASS HighScoreForm - same as HighScoreActivity


Version 4.0

Visit game page at http://rh.fidrelity.at/
Licensed under CC BY-NC-SA: http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_US


You may NOT copy this game and upload a the copy on Google Play. 
If you want to contribute please feel free to commit changes to the code base to https://github.com/mwebi/runnersHigh .

