# Balancing Parameters
## 1. Number of Iterations
   - If the iterations is more than 1 then only the worst possible team will be printed + no extra balancing debug.
   - The average and max weight difference will be printed at the end.
## 2. Number of Players 
## 3. Balance Method 
   - **Filters** - Filters to apply on the group of players to be balanced.
      - Supply a list of filters to iterate through to pass into the balancing method. (This can leave out players so ensure the list of filters accounts for all players)
      - Default List (Simplified): isDefender, isCryomancer, isTank, isDamage, isHealer
   - [**Methods**](src/main/java/com/ebicep/warlordsbalancer/BalanceMethod.java) - Iterates through each filter + their set of player sorted by weight and picks which team a player should go to.
      - **V1** - Places the next player onto the team with the lowest weight.
         - Debug: (Index Added)
      - **V2** - Places the next player in an alternating fashion.
         - Debug: (Index Added)
      - **V2.1** - Same as V2 but during a transition to a new filter, the alternating series begins at the team with the lowest weight.
         - Debug: (Index Added) ([Team]:[Team's Weight]->[Team's Weight after being added, if added to])
## [4. Weight Generation Method](src/main/java/com/ebicep/warlordsbalancer/WeightGenerationMethod.java) (Min: 0.43, Max: 4)
   - **Random** - Randomly generates a weight between min and max.
   - **Normal Distribution** - Randomly generates a normally distributed weight using supplied mean and standard deviation.
     - Default Mean = (Max - Min) / 2
     - Default Standard Deviation = 1.2
   - **Custom** - Weight * (1 + sqrt(Last 10 W/L))
     - Weight = (Last 100 Wins) / (Last 100 Losses)
       - Last 100 Wins = Randomly generated number 0-100
       - Clamped between min/max
   - **Custom with Normal Distribution** - Weight * (1 + sqrt(Last 10 W/L))
     - Weight = (Last 100 Wins) / (Last 100 Losses)
       - Last 100 Wins = Randomly generated normally distributed number using supplied mean and standard deviation.
         - Default Mean = 50
         - Default Standard Deviation = 25
       - Clamped between min/max
## [5. Extra Balance Features](src/main/java/com/ebicep/warlordsbalancer/ExtraBalanceFeature.java)
   - **Swap Uneven Teams** - Swaps players between the two teams to even out the player count.
      - Only tries to swap players that have a spec type whose count isnt even between the two teams. (Ignored if need be)
      - Only tries to swap player which would even out the weights the most.
      - Debug: (MOVED)
   - **Swap Spec Types** - Swaps players between the two teams to even out the spec type weights.
      - First gets the spec type with the most difference in weight.
      - Then gets the players with the spec type on the two teams.
      - Then finds a swap that would even out the spec type weights the most.
      - Players swapped indicated by "SWAPPED".
      - This repeats 5 times or until no more swaps can be made.
      - Debug: (SWAPPED #[INDEX])
   - **Swap Team Spec Types** - Swaps groups of players with matching spec types between the two teams to even out the total weight between the teams.
      - First gets the spec type with the most difference in weight (ST) and the team with the higher value (T).
      - Then swap group of spec type players with the lowest weight onto T. (Only if swapped group and ST has equal number of players on each team)
      - Example:
        - BLUE = (0, 100, 50), RED = (50, 0, 0)
        - BLUE = (0, 100, 0), RED = (50, 0, 50)
   - **Compensate** - Assuming spec types are as already even as they can be. For the case where a spec type weights are very unbalanced, so to compensate, swap other spec types.
      - Only works if weight diff is greater than 1.
      - First takes the highest spec type weight difference = x. (Max Weight Diff)
      - Then looks at different spec types and finds a player on higher weighted team to swap with another player on the lower weighted team who is lower weight and chooses the highest difference closest to (x/2 + Bonus). (Highest Weight Diff)
        - Bonus = Adjustable value to allow for closer swaps. (Default: 1)
        - Example:
          - BLUE = 50 >> 25 + 10 + 7 + 5 + 3
          - RED = 40 >> 20 + 10 + 6 + 4 + 1
          - x = 50 - 40 = 10
          - x/2 = 10/2 = 5
          - x/2 + Bonus = 5 + 1 = 6
          - Swaps BLUE 7 and RED 1
            - 7-1 = 6
            - BLUE = 44 >> 25 + 10 + 1 + 5 + 3
            - RED = 46 >> 20 + 10 + 6.5 + 2.5 + 7
      - Debug: (COMPENSATE SWAP #[INDEX] >> [Highest Team] >> [Old Team 1 Weight]|[Old Team 2 Weight] >> [New Team 1 Weight]|[New Team 2 Weight] >> [Max Weight Diff]|[Highest Weight Diff])
   - **Hard Swap** - Finds best swap between to players with matching spec types that would even out the teams weights
      - Essentially the same as Compensate but swapping all spec types.
      - Debug: (HARD SWAP #[INDEX] >> [Highest Team] >> [Old Team 1 Weight]|[Old Team 2 Weight] >> [New Team 1 Weight]|[New Team 2 Weight] >> [Max Weight Diff]|[Highest Weight Diff])
> [!NOTE]
> "Debug:" Refers to the text next to each player indicating what action was taken upon them during balacing.

