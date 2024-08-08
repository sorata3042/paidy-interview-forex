<img src="/paidy.png?raw=true" width=300 style="background-color:white;">

# Paidy Forex rates proxy

## Overview

[Forex](forex-mtl) is a simple application that acts as a local proxy for getting exchange rates from [One-Frame service](https://hub.docker.com/r/paidyinc/one-frame). It's a service that can be consumed by other internal services to get the exchange rate between a set of currencies, so they don't have to care about the specifics of third-party providers.

### Service Requirements

1. Exchange Rate Retrieval: The service returns an exchange rate when provided with 2 supported currencies
2. Rate Freshness: The rate should not be older than 5 minutes
3. Request Capacity: The service should support at least 10,000 successful requests per day with 1 API token

### Known Constraints

The following is a notable drawback of the [One-Frame service](https://hub.docker.com/r/paidyinc/one-frame): 

> The One-Frame service supports a maximum of 1000 requests per day for any given authentication token. 

## Implementation

In order to fulfill the 10,000 request requirement with a single token, a caching solution was implemented to store of the Rates obtained from One-Frame service.

### Cache Specifications
- Cache Type: Caffeine
    - Caffeine was chosen due to it being a performant in-memory caching solution. 
- Cache Expiration: 1.5 minutes min, 5 minutes max
    - An expiration of 5 minutes ensures that a Rate is not stale and the One-Frame calls is lowest while the minumum of 1.5 minutes ensures the freshest Rates with calls to One-Frame service nearing the limit. 
- Cache Size: 161
    - The current Currency values accepted by One-Frame - 1, ensures all rates from a single currency can be stored.
    - i.e. all (from: JPY, to: any) rates can be stored.

### Logic Overview

The following details the processes with a call to the Forex Proxy rate API.

1. Same Currency Check:
    - In the case that the `from` and `to` currencies are the same, a standard rate of 1 is returned. This avoids unnecessary processing

2. Initial Cache Lookup:
    - Check if any value is stored within the cache

3. Cache Miss Handling:
    - If a cached Rate is not found in the cache, a call to One-Frame service is performed to retrieve all Rates from JPY, the chosen default intermediary rate, which are subsequently stored within the cache

4. Rate Calculation:
    - Using the newly populated cache rates, the desired rate gets calculated. Desired rates are calculated as follows:
        1. From JPY: All from JPY rates are stored within the cache, so the cached rate is returned
        2. To JPY: The inverse of the desired Rate, the cached from JPY Rate, is used to calculate the desired rate, 1/(from JPY price)
        3. Not JPY: The desired rate does not include the default JPY currency, so two cached prices are used to calculate the desired rate, (from: JPY, to: desired to) / (from: JPY, to: desired from)

With this implementation, a call to One-Frame service will need to occur when all cache entries expire. Since there is a total of 1440 minutes in a day, there would be a max of 288 calls to One-Frame service with a 5 minute cache entry expiry —well within One-Frame call constraints. At the 1.5 minute expiry, there would be a total of 960 calls to the service —also within the 1000 limit. Moreover by caching the rates, the Forex Proxy is able to handle the Request Capacity requirement by constantly being able to output rates as long as One-Frame service is responding.

### Pros
1. Ease of implementation
    - Caffeine is a simple, easy to implement in-memory solution storage strategy
2. High performance
    - Low latency with cached Rate retrieval
    - High volumne handability with the caching ensures a large number of request to the Forex Proxy can be handled efficiently
3. Efficient data storage
    - With the storage of all to Rates for a given currency and the calculation for all other Rates, the storage required is `n - 1`, where `n` is the number of currencies supported. Thus, an increase in the amount of currencies supported would increase the required storage by the same amount
    - In contrast, storing each currency pair permutation requires a storage amount of `n * (n - 1)`

### Cons
1. Data inconsistency across multiple instances
    -  Across multiple Forex Proxy instances, the Caffeine local memory cache would not be consistent
2. Synchronous Rate request to One-Frame service
    - The Rate get call to the Forex Proxy with a cache miss performs calls One-Frame service synchronously to cache refresh

### Potential Improvements

The following would be potential solutions to the Cons presented:
1. Implement External caching solution
    - Expanding the Forex Proxy neccesitates the implementation of a centralized external cache solution, such as Memcached or Redis, in order to ensure consistency
2. Implement scheduler
    - Implementing a scheduled asynchronous refresh would ensure the API response time remains low by eliminating the direct call to One-Frame service. Moreover, the cache would be able to be constantly populated instead of having to wait for a request

## Getting Started

### Prerequisites

#### One-Frame Service
Pull the docker image
`docker pull paidyinc/one-frame`

Run the service
`docker run -p 8080:8080 paidyinc/one-frame`

### Usage

#### Forex Proxy
Run the service
`sbt run`

#### Example cURL request
> curl -i "localhost:8081/rates?from=USD&to=JPY"
>
> [{"from":"USD","to":"JPY","price":0.35140336833848823,"timestamp":"2024-08-07T01:14:30.190Z"}]

### Testing
Run the service
`sbt test`