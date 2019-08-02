# Roundtrip2RileyLinkAAPS

This is fork from original Roundtrip2 branch, which was intended as Pump Driver for HAPP application. If you need driver for HAPP please go to [this repository](https://github.com/TC2013/Roundtrip2)

This is repository with App where we will test connectivity with Medtronic via RileyLink. After this is done, we might use the same repository to continue Omnipod development.

As you can see I created Project - Medtronic here, with nice board with task, so that you can follow what was done, and what is beeing done at the moment.

*Any help is welcome.* As soon as I am so far, parts of this App and also parts of AAPS (integration will be done in [andyrozman/AndroidAPS](https://github.com/andyrozman/AndroidAPS) repository in branch riley_link_medtronic) will be available for testing (look in Project board into column - Done - "Ready for testing". Code needs to be tested on different pumps, I will be doing development with 712, and I am hoping I will be able to implement everything with it.

# About the app

I did little reafactoring of the GUI so that "our" options are more visible. On start page there is "Show AAPS" button, that will switch you to our test screen, where we have buttons for all commands we intend to implement. Commands supported will be colored green, the one in work yellow, and others are normal. History will probably be tested from main screen only.

We are starting from this app, because communication with RileyLink here already works, and that is what is needed for communciation with Medtronic pump (and later also with Omnipod). After we have commands working, we will start with refactoring of RileyLink code, so that it can be used by more than one consumer/pump.

This is the old gitter, but I plan to create new room, as soon I am past Milestone 1.
[![Gitter](https://badges.gitter.im/TC2013/Roundtrip2.svg)](https://gitter.im/TC2013/Roundtrip2?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Omnipod
In the dev_omnipod branch we are creating an Omnipod driver for AAPS by porting [Loop's driver for Omnipod](https://github.com/ps2/rileylink_ios/tree/omnipod-testing).
