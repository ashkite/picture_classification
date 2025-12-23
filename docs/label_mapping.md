# Event Label Mapping (Draft)

Goal: map dataset labels to app event categories with precision-first rules.

## Categories
- Travel/Tourism
- Nature/Outdoor
- City/Street
- Food/Restaurant
- Cafe/Dessert
- Party/Celebration
- Wedding
- Family/Kids
- Meeting/Work
- Performance/Stage
- Sports/Fitness
- Animals/Pets
- Beach/Sea
- Mountain/Hiking
- Night/Evening

## Mapping rules
- Use strict keyword or dataset-specific labels.
- If label is ambiguous, map to none (unknown).
- Avoid mapping objects directly to events unless clearly tied.

## Example mapping (keywords)
- Travel/Tourism: landmark, temple, palace, monument, museum, tourism
- Nature/Outdoor: forest, lake, field, park, waterfall
- City/Street: street, city, downtown, subway, skyline
- Food/Restaurant: food, dish, restaurant, dining, table
- Cafe/Dessert: coffee, cafe, dessert, pastry
- Party/Celebration: party, birthday, celebration, balloons
- Wedding: wedding, bride, groom, ceremony
- Family/Kids: family, child, baby, playground
- Meeting/Work: office, meeting, conference, workstation
- Performance/Stage: concert, stage, theater, performance
- Sports/Fitness: stadium, sport, gym, workout
- Animals/Pets: dog, cat, pet, animal
- Beach/Sea: beach, sea, ocean, coast
- Mountain/Hiking: mountain, hiking, trail, hill
- Night/Evening: night, city lights, nightscape

## Dataset notes
- Places365: use scene labels (e.g., beach, forest, office)
- Open Images: use object labels cautiously (food, dog, cat)
- Food-101: map directly to Food/Restaurant

## Threshold policy
- Default threshold: 0.8
- If multiple categories pass threshold, take the top-1 only
