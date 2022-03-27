# REST Tester

A demo project for testing REST endpoints utilizing `clojure.spec.alpha`.  

The goal is to spec out the desired payload for a foreign API endpoint and use the generative features of `spec` to generate such a payload.

The last step would be to send that payload to said endpoint.

## Config

There is a `src/sample_config.clj` provided to show what is supported.  
Fill in your real config in `src/config.clj` and require this in `src/core.clj` but don't commit that file!

## Deps

I realized that spec has some trouble generating strings that also conform to a given regex.  
That's why I added the dependency [strgen](https://github.com/miner/strgen "Github") which generates strings from gegular expressions.

To make the API call I use the [http-kit](https://github.com/http-kit/http-kit "Github") client.
