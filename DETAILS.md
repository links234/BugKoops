# Goals
This application was developed mainly because there is no other way to use already standard apps from
Google Play. This is due to the fact that QR codes really can't encode more than 200 bytes in reality
and Oops messages are 1.5kb on average. The fact that it include report editing, sharing, integration
with Bugzilla or any other feature no matter how big it is does not imply that they where the final
target. Those were added solely on the idea of simplifying/merging the steps need to report an Oops.
So please, try to add features that will only make life easier for the users or at least won't make
it harder than it already is.

The ideal workflow (which is not completed yet) is to setup a configuration profile first time you
launch the app (where to send to scanned reports, patterns to search for in the message, title, etc.)
and from there on only to press "Scan", point the device camera to the QRs and just wait, letting
the app to do everything else!

## Application design
The choice of activity-fragment pairs is in case we want to support two-pane UI for tables. Every
component is decoupled from everything else as much as it is possible. And everything that falls
in the category of "multiple ways to do same thing" is layered in interface-implementation, nothing
fancier than this!

However, here is a brief components and sources files tree overview, as good as it can be since the
relations or more complex:

1. UI backbone
    * **MenuActivity** - base class for every other activity that needs a menu (all except
    **ScannerActivity**)
    * **MainActivity**
        * **MainActivityFragment**
    * **ReportActivity**
        * **ReportListFragment** and **ReportListAdapter**
        * **ReportDetailActivity** and **ReportDetailFragment**
    * **SettingsActivity** - right now only have options related to scanner
        * **ResetPreferencesDialog**
    * **BugzillaSendActivity** - dialog for sending Bugzilla reports
2. Storage and caching
    * **Data\\**: uses SQLite for storage
        * **BugKoopsContract** - conventions and constants
        * **BugKoopsDbHelper** - initialization and version upgrading for database
        * **BugKoopsProvider** - the actual implementation
3. Data processing
    * **Data\\**: divided into 3 components:
        * **PacketManager** - you can push a packet here to be decoded and if it's detected as a
        multi-part message will push it further to **MessageManager** otherwise it will be treated
        as a full message end sent to **ReportManager**
        * **MessageManager** - responsible for merging the packets and sending complete message
        further to **ReportManager**
        * **ReportManager** - responsible for store, processing and auto-send the report to the
        world (EasyShare, Bugzilla, etc.)
4. Scanner
    * Uses [ZXing Android Embedded][1]
    * **ScannerActivity** -> continuous scanning for QR codes (in fact you can use any barcode
    supported by [ZXing][2]) and pushing them to **PacketManager**
5. Integrations
    * **Integrations\\** - here comes the whole implementation in a separate folder for each
    integration
        * **Bugzilla\\** - this is the only integrations right now (and maybe the most useful)
            * **BugzillaAPI** - because Bugzilla support multiple formats of interaction we will
            introduce this "standard" set of operations
            * **BugzillaAutoDetect** - for your ease, you should use this class to connect to a server
            and the object implementing **BugzillaAPI** for communication
            * **BugzillaREST** - REST is the only fully supported API in the latest Bugzilla version
            * **BugzillaXMLRPC** - unfortunately this is the only API supported on [kernel.org][3]
            version
6. Misc
    * **UI\\**: this is where you should add all dynamic UI elements
        * **ScanButton** - fancy circle button with animation used by **MainActivity**
    * **Network\\**:
        * **HttpConnection** - generic http/https connection with POST/GET
        * **NoSSLv3SocketFactory** - apparently there is some bug in Android SDK + Https and this
        is to workaround this problem. Thanks to Bhavit Singh Sengar.
    * **OnTaskCompleted** - just a callback interface
    * **Utility** - various not-so-specific-code
    * **XMLRPCBuilder** - helper class

If you want to understand how the 1-way multi-part message works, please read [BK1 format][4].

For a better view and more relations between the components see this [diagram][5].

## Coding style
Just use Android Studio code prettify, more important is the problem abstraction.

[1]: https://github.com/journeyapps/zxing-android-embedded
[2]: https://github.com/zxing/zxing/
[3]: https://bugzilla.kernel.org/
[4]: FORMAT.md
[5]: diagram.png