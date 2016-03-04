## How to contribute

- Pull request should be made against the `development` branch until the milestone is reached and v2.0.0 is pushed into production.
- Breakdown commits and pull request to make review processes as fast as possible. For example do not create a single pull request for multiple issues/tasks.

##Building

- Import the project into Android Studio and build it just like any other android project.
- Create a file called `keys.properties` in the root folder of the project.
- Copy the template below into the file and replace the dummy keys with your own API keys in order to make certain functions to work. You don't need to register an API key if you won't use these functions.

```
API keys
BACKPACK_TF_API_KEY=DUMMY_KEY
STEAM_WEB_API_KEY=DUMMY_KEY
```

- You need a backpack.tf API key (http://backpack.tf/api/register) for the price history function to work.
- You need a steam web API key (http://steamcommunity.com/dev/apikey) in order to be able to download steam user data.

## No Coding
- You can report issues or suggest new features in the [Steam group](http://steamcommunity.com/groups/bptfandroid) or [here on Github](https://github.com/Longi94/bptf/issues)
