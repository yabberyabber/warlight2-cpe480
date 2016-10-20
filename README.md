# warlight2-cpe480
Team "It's not a phase, mom!"'s entry into the warlight2 ai challenge 

Includes a handful of bots to test against and a makefile to build bots and run test matches.  To run a test match run the following commane in the base directory:

`make fight`

This will pit the RandomBot against the RandomBot.  To stage a fight between two specific bots, run set the BOT and BOT2 variables to the name of the bot (the name of the bot is the directory it lives in.  For example AdvancedBot and RandomBot).  Couple examples:

`make fight BOT=AdvancedBot`

Will fight AdvancedBot against RandomBot

`make fight BOT=AdvancedBot`

Will fight AdvancedBot against AdvancedBot
