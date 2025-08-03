
ALTER TABLE dish_reactions DROP CONSTRAINT dish_reactions_reaction_type_check;
-- Add a new check constraint that allows all reaction types
ALTER TABLE dish_reactions ADD CONSTRAINT dish_reactions_reaction_type_check 
CHECK (reaction_type IN ('HEART', 'DELICIOUS', 'AVERAGE', 'BAD'));
