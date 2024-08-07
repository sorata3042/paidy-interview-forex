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
- Cache Size: 72
    - The cache size of 72 ensures that the amount of permutations of Currency pairs can be stored.

### Logic Overview

The following details the processes with a call to the Forex Proxy rate API.

1. Same Currency Check:
    - In the case that the `from` and `to` currencies are the same, a standard rate of 1 is returned. This avoids unnecessary processing

2. Initial Cache Lookup:
    - Check if the requested Pair exists within the cache and return if present

3. Cache Miss Handling:
    - If the desired Rate is not found in the cache, a call to One-Frame service is performed to retrieve all possible Rates which are subsequently stored within the cache

4. Final Cache Lookup:
    - The newly populated desired cached Rate is returned

With this implementation, a call to One-Frame service will need to occur when all cache entries expire. Since there is a total of 1440 minutes in a day, there would only be a maximum of 288 calls to One-Frame service --well within Rate Capacity requirement. At the 1.5 minute expiry, there would be a total of 960 calls to the service. Moreover, the caching solution enables the Forex Proxy to be able to handle over 10,000 requests within the day.

### Pros
1. Ease of implementation
    - Caffeine is a simple, easy to implement in-memory solution storage strategy
2. High performance
    - Low latency with cached Rate retrieval
    - High volumne handability with the caching ensures a large number of request to the Forex Proxy can be handled efficiently

### Cons
1. Ineffiecient data storage
    - Although Caffeine is  efficient, caching all currency pair combinations can consume a significant amount of memory. If the number of Currencies grows, the total number of permutations would grossly increase
        - Currently, the number of currencies is low, so the storage does not consume a significant amount of memory.
2. Data inconsistency across multiple instances
    -  Across multiple Forex Proxy instances, the Caffeine local memory cache would not be consistent. 
3. Synchronous Rate request to One-Frame service
    - The Rate get call to the Forex Proxy with a cache miss performs calls One-Frame service synchronously to cache refresh

### Potential Improvements

The following would be potential solutions to the Cons presented:
1. Modify caching
    - Instead of storing each permutation, potentially all Rates stored can be centralized against one Currency and the rates for different currencies can be calculated using those values stored. 
        - For example with the format of `(from, to, rate)` given that `(USD, JPY, 0.5)` and `(USD, CAD, 2)`, CAD to JPY would have a calculated conversion rate of 4
2. Implement External caching solution
    - Expanding the Forex Proxy would need to implement an external cache solution such as Memcached or Redis in order to ensure consistency
3. Implement scheduler
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
> curl  -i "localhost:8081/rates?from=USD&to=JPY"
>
> [{"from":"USD","to":"JPY","price":0.35140336833848823,"timestamp":"2024-08-07T01:14:30.190Z"}]

### Testing
Run the service
`sbt test`