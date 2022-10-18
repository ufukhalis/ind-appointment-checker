# IND Appointment Checker

### What is IND?

---
The Immigration and Naturalisation Service (IND) assesses all residence permit applications from the people who want to live, work or study in the Netherlands.

### Purpose

---
The purpose of this project to help you find available appointment slots and notify you via some messaging apps(such as WhatsApp, Telegram etc.)
Due to increasing demand, there is hard to find a slot immediately for your needs in IND website, but there is always a chance to catch some spaces near future and `IND Appointment Checker` app does that for you with given period and filter.

### How to use?

---
The application itself uses JVM to be run, you can download the JAR file from the [releases](https://github.com/ufukhalis/ind-appointment-checker/releases) section(good to have latest JRE installed in your env.) or clone it for some manual work or you can try to run it via docker since the application available in public docker registry :)

`IND Appointment Checker` is a console application so when you run the JAR file, you should pass some arguments to it. You can see available arguments running below command.

```shell
java -jar ind-appointment-checker-1.2.0.jar -h

--messagingType, -t -> Messaging Type(whatsApp, telegram) (always required) { String }
--whatsAppApiKey, -wp-key -> WhatsApp Api Key { String }
--whatsAppPhoneNumber, -wp-pn -> WhatsApp Phone Number { String }
--filterDate, -fd -> Filter date (always required) { String }
--indLocationString, -l -> IND Locations(AMSTERDAM, DEN_HAAG, RIJSWIJK_TEMP, ZWOLLE, DEN_BOSCH, HAARLEM, EXPAT_CENTER_GRONINGEN, EXPAT_CENTER_MAASTRICHT, EXPAT_CENTER_WAGENINGEN, E
XPAT_CENTER_EINDHOVEN, EXPAT_CENTER_DEN_HAAG, EXPAT_CENTER_ROTTERDAM, EXPAT_CENTER_ENSSCHEDE, EXPAT_CENTER_UTRECHT, EXPAT_CENTER_AMSTERDAM) (always required) { String }
--indProductString, -pd -> IND appointment typesRESIDENCE_DOCUMENT, BIOMETRIC, RESIDENCE_STICKER, RETURN_VISA (always required) { String }
--period, -p [30] -> Checking period in seconds { Int }
--help, -h -> Usage info
```

Before run the actual command, you need to prepare your phone from this link to be able to get messages, follow the below links. 

* WhatsApp Integration -> https://www.callmebot.com/blog/free-api-whatsapp-messages/
* Telegram Integration -> https://www.callmebot.com/blog/telegram-text-messages/

> Currently, IND Appointment Checker application only supports WhatsApp and Telegram, later on there will be other integration as well.

And then, we can build the actual run command like below

```shell
# WhatsApp Usage
java -jar ind-appointment-checker-1.2.0.jar -t whatsApp -wp-key {yourApiKey} -wp-pn {yourWhatsAppPhoneNumber} -fd 2022-12-30 -l AMSTERDAM -p 15 -pd BIOMETRIC
```

Or

```shell
# Telegram Usage
java -jar ind-appointment-checker-1.2.0.jar -t telegram -tl-username {yourTelegramUserName} -fd 2022-12-30 -l AMSTERDAM -p 15 -pd BIOMETRIC
```

via Docker

```shell
docker run ufukhalis/ind-appointment-checker -t whatsApp -wp-key {yourApiKey} -wp-pn {yourWhatsAppPhoneNumber} -fd 2022-12-30 -l AMSTERDAM -p 15 -pd BIOMETRIC
```

> The explanation of above command is that check a BIOMETRIC appointment in AMSTERDAM lower than this date 2022-10-30 for each 15 SECONDS and if there is one then send a message via WhatsApp or Telegram(Depends on which type of messaging you choose).

> Once an appointment slot found, then the application will exit. So, you need to run it again to find new one.

## Important

---

Please do not abuse the IND system with given small period of time, put some reasonable values. Also, please share your ideas/issues to make this app better.

### What's next

---
There is a possibility that I may create a website version of this application so everybody that can reach easily.
