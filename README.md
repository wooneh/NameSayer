# NameSayer
In today’s multi-cultural world, people are often coming in contact with names that they are uncertain how to pronounce, and/or are having to put up with people saying their own names incorrectly. The project involves creating a platform that will provide users with an application to help them practise unfamiliar names.

NameSayer is targeted at teachers and lecturers to practice the names of the students in their classes.

## Installation and Requirements
Requirements: FFMPEG added to path, suitable microphone and speaker/headphones for audio playback. Java 8 or later.
Download the provided .zip file and extract the contents. Run the .jar file from the command line: java -jar NameSayer.jar

## The Names Database
The Names Database contains recordings of various names from around the world. The Database is bundled with the application.

## Combining Names
The user can input a series of names representing first, middle, or family names that are separated by a space or hyphen. NameSayer will 
retrieve each part of the name from the database where available, and combine the audio together for playback. The audio is then processed by removing silence and normalizing the volume.

## Recording Names
When the user selects a new name, they have the option to record themselves pronouncing the name after hearing the processed audio. A level meter is displayed so that the user can control the volume of their voice, or to check that their microphone is working. 

## Practicing Names
The user can practice a list of names of a class or a single name. In class mode, the user can quickly navigate through each name and play a recording of that name.

## Speech Playback
The user can play their own attempt of the name and compare it with that in the database. Should they choose to, they have the option to save their attempt and access it in the future.

## Bad Quality Recordings
If a recording from the database is of bad quality, the user can flag it as such. 

## Choosing Recordings
NameSayer will attempt to choose recordings of a name that are not bad quality, if such a recording is available.

## User Manual
A user help manual is contained within the application, as well as tooltips to help the user navigate.

## Reward
The user is able to see their progress in practicing the names in their class. When they have finished practicing each name, NameSayer recognizes their achievement with a pat on the back.

## Accessibility
NameSayer uses the default user-friendly colours of their operating system. This ensures that all users have an enjoyable experience using NameSayer.

# Special features:
## Compatibility
The application is compatible on both Linux and Windows, provided they have FFMPEG installed on the path.

## Saving Classes
NameSayer remembers and saves the students in your course. You don't have to re-upload your class list every time you open the application, and NameSayer remembers your past attempts.
