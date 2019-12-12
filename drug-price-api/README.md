**Backend Spring Boot API**

_*Developing Locally*_

In your command prompt, type:
  
  **1.** _git clone_ [_https://github.com/danvalinotti/DrugPricingBackend.git_](https://github.com/danvalinotti/DrugPricingBackend.git)
  
  **2.** _cd DrugPricingBackend_
  
  **3.** _git fetch --all_
  
  **4.** Open the folder _drug-price-api_ in your IDE (Eclipse, IntelliJ etc.)
  
  **5.** Create run configurations
  
    Spring Boot: DrugPriceApiApplication.class
  
    Gradle task: bootJar

**Building for Production**
  1. Run _bootJar_ gradle task
  2. .jar output is generated in _build/libs_