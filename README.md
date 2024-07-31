<img src="/paidy.png?raw=true" width=300 style="background-color:white;">

# Paidy Forex rates proxy

[Forex](forex-mtl) is a simple application that acts as a local proxy for getting exchange rates from [One-Frame service](https://hub.docker.com/r/paidyinc/one-frame). It's a service that can be consumed by other internal services to get the exchange rate between a set of currencies, so they don't have to care about the specifics of third-party providers. I

The following is a notable drawback of the [One-Frame service](https://hub.docker.com/r/paidyinc/one-frame): 

> The One-Frame service supports a maximum of 1000 requests per day for any given authentication token. 
