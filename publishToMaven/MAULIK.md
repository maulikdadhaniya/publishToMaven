Add this whole Folder "publishToMaven" in project

and then trigger below command to publish the library to maven local


```shell
cd /Users/maulikdadhaniya/Documents/Maulik/Product/ToastX                                                   
cp publishToMaven/secrets.properties.example publishToMaven/secrets.properties
./publishToMaven/publish-central.sh
```
Then trigger below command to publish the library to maven central

```shell
cd /Users/maulikdadhaniya/Documents/Maulik/Product/ToastX
chmod +x publishToMaven/publish-central.sh
./publishToMaven/publish-central.sh
```