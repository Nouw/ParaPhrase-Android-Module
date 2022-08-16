# ParaPhrase Android Module

Android Module to connect to the ParaPhrase server for Medical Transcribing

## Installation

Add the following line in `settings.gradle`

```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' } // Add this line
        ...
    }
}
```

Then go to the `build.gradle` of your android app, and add
```
dependencies {
    ...
    implementation 'com.github.Nouw:ParaPhrase-Android-Module:TAG'
}
```

At last, sync your gradle files.

## How to use

You can start using ParaPhrase by creating the ParaPhrase object. Below are the parameters given.

|Parameter name   |Required   |Description   |
|-----------------|-----------|--------------|
|context          | Yes       | App contect to ask permission to record audio            |
|listener         | Yes       | Function to handle incoming messages                     |
|url              | False     | URL to the ParaPhrase Server
|medical          | False     | Yes if ParaPhrase should give back the medical transcription|

## Contact

For contact about this project, post an issue or contact send an email to fabio@prometech.eu