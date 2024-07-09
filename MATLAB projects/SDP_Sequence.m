clc
clear


% Initialize scene
my_scene = simpleGameEngine('retro_cards.png',16,16,5); 
play_again = 1;
while play_again == 1
    % Set up variables to name the various sprites
    outline = 7;
    empty_sprite = 75;
    row1 = 22:29;
    row2 = 31:40;
    row3 = 41:50;
    row4 = 51:60;
    row5 = 61:70;
    blue_sprite = 78;
    red_sprite = 77;
    corner = 21;
    sequence = 92:99;
    star = 82;
    
    % Display empty board   
    board_display = empty_sprite * ones(14,14);
    board_display(2:13,2) = outline;
    board_display(2,2:13) = outline;
    board_display(13,2:13) = outline;
    board_display(2:13,13) = outline;
    board_display(2,4:11) = sequence;
    board_display(4:11,2) = sequence;
    board_display(13,4:11) = sequence;
    board_display(4:11,13) = sequence;
    array1 = ones(14);
    array1(3,4:11) = row1.*array1(3,4:11);
    array1(4,3:12) = row2.*array1(4,3:12);
    array1(5,3:12) = row3.*array1(5,3:12);
    array1(6,3:12) = row4.*array1(6,3:12);
    array1(7,3:12) = row5.*array1(7,3:12);
    array1(12,4:11) = row1.*array1(12,4:11);
    array1(11,3:12) = row2.*array1(11,3:12);
    array1(10,3:12) = row3.*array1(10,3:12);
    array1(9,3:12) = row4.*array1(9,3:12);
    array1(8,3:12) = row5.*array1(8,3:12);
    array1(3,3) = corner.*array1(3,3);
    array1(3,12) = corner.*array1(3,12);
    array1(12,3) = corner.*array1(12,3);
    array1(12,12) = corner.*array1(12,12);
    drawScene(my_scene,board_display,array1)
    
    %setting up the initial deck of cards and cards for each player 
    %creating an array of 2 decks of cards with each number reperesenting a card
    cards = [];
    for i = 1:52
        for j = 1:2
            cards = [cards,i];
        end
    end
    %creating an array with the sprites of each card
    cards_array = [33,34,47,60,22,35,48,61,23,36,49,62,24,37,50,63,25,38,51,64,26,39,52,65,27,40,53,66,28,41,54,67,29,42,55,68,46,43,56,69,70,57,44,31,59,58,48,32,89,88,87,86];
    
    %making 2 random arrays which will be used to make an array of 6 random cards from the deck while updating the original deck of cards
    player_1_shuffle = randperm(length(cards),6);
    player_1_cards = cards_array(cards(player_1_shuffle));
    cards(player_1_shuffle) = [];
    player_2_shuffle = randperm(length(cards),6);
    player_2_cards = cards_array(cards(player_2_shuffle));
    cards(player_2_shuffle) = [];
    array1(1,5:10) = player_1_cards.*array1(1,5:10);
    array1(14,5:10) = player_2_cards.*array1(14,5:10);
    
    %creating and adding 2 kings on each side of the board which will act as buttons when a player wants to use their king card
    red_kings_buttons = [86;87];
    black_kings_buttons = [88;89];
    array1(7:8,1) = red_kings_buttons.*array1(7:8,1);
    array1(7:8,14) = black_kings_buttons.*array1(7:8,14);
    drawScene(my_scene,board_display,array1)
    red_kings = [86,87];
    black_kings = [88,89];
    
    
    %card sprites
    
    king_red_heart = 86;
    king_red_diamond = 87;
    king_black_club = 88;
    king_black_spade = 89;
    queen_red_heart = 32;
    queen_red_diamond = 45;
    queen_black_club = 58;
    queen_black_spade = 59;
    jack_red_heart = 31;
    jack_red_diamond = 44;
    jack_black_club = 57;
    jack_black_spade = 70;
    one_red_heart = 33;
    one_red_diamond = 34;
    one_black_club = 47;
    one_black_spade = 60;
    two_red_heart = 22;
    two_red_diamond = 35;
    two_black_club = 48;
    two_black_spade = 61;
    three_red_heart = 23;
    three_red_diamond = 36;
    three_black_club = 49;
    three_black_spade = 62;
    four_red_heart = 24;
    four_red_diamond = 37;
    four_black_club = 50;
    four_black_spade = 63;
    five_red_heart = 25;
    five_red_diamond = 38;
    five_black_club = 51;
    five_black_spade = 64;
    six_red_heart = 26;
    six_red_diamond = 39;
    six_black_club = 52;
    six_black_spade = 65;
    seven_red_heart = 27;
    seven_red_diamond = 40;
    seven_black_club = 53;
    seven_black_spade = 66;
    eight_red_heart = 28;
    eight_red_diamond = 41;
    eight_black_club = 54;
    eight_black_spade = 67;
    nine_red_heart = 29;
    nine_red_diamond = 42;
    nine_black_club = 55;
    nine_black_spade = 68;
    ten_red_heart = 46;
    ten_red_diamond = 43;
    ten_black_club = 56;
    ten_black_spade = 69;
    
    
    %card numbers
    king_red_heart_num = 52;
    king_red_diamond_num = 51;
    king_black_club_num = 50;
    king_black_spade_num = 49;
    queen_red_heart_num = 48;
    queen_red_diamond_num = 47;
    queen_black_club_num = 46;
    queen_black_spade_num = 45;
    jack_red_heart_num = 44;
    jack_red_diamond_num = 43;
    jack_black_club_num = 42;
    jack_black_spade_num = 41;
    one_red_heart_num = 1;
    one_red_diamond_num = 2;
    one_black_club_num = 3;
    one_black_spade_num = 4;
    two_red_heart_num = 5;
    two_red_diamond_num = 6;
    two_black_club_num = 7;
    two_black_spade_num = 8;
    three_red_heart_num = 9;
    three_red_diamond_num = 10;
    three_black_club_num = 11;
    three_black_spade_num = 12;
    four_red_heart_num = 13;
    four_red_diamond_num = 14;
    four_black_club_num = 15;
    four_black_spade_num = 16;
    five_red_heart_num = 17;
    five_red_diamond_num = 18;
    five_black_club_num = 19;
    five_black_spade_num = 20;
    six_red_heart_num = 21;
    six_red_diamond_num = 22;
    six_black_club_num = 23;
    six_black_spade_num = 24;
    seven_red_heart_num = 25;
    seven_red_diamond_num = 26;
    seven_black_club_num = 27;
    seven_black_spade_num = 28;
    eight_red_heart_num = 29;
    eight_red_diamond_num = 30;
    eight_black_club_num = 31;
    eight_black_spade_num = 32;
    nine_red_heart_num = 33;
    nine_red_diamond_num = 34;
    nine_black_club_num = 35;
    nine_black_spade_num = 36;
    ten_red_heart_num = 37;
    ten_red_diamond_num = 38;
    ten_black_club_num = 39;
    ten_black_spade_num = 40;
    
    %starting the game 
    winner = 0;
    player_turn = 1;
    
    while winner == 0 %initializing a value for winner so that the turns go on until a winner is decided
        while player_turn == 1 %player 1 turn
            [r,c] = getMouseInput(my_scene); %mouse input from the player
            no_card = 0;
            no_red_king = 0;
            no_black_king = 0;
            no_chip = 0;
            
            if board_display(r,c) == red_sprite %when there is already a chip at the selected space
                fprintf("place the chip on an empty space\n")
                no_chip = 1;
            %playing a turn without using any kings
            elseif board_display(r,c) ~= red_sprite && (array1(r,c) ~= red_kings(1) && array1(r,c) ~= red_kings(2) && array1(r,c) ~= black_kings(1) && array1(r,c) ~= black_kings(2))
                %loop to check all 6 cards that the player has
                for p = 1:6 
                    %comparing selected spot on the boards and cards in the player's deck and updating the main deck 
                    if player_1_cards(p) == array1(r,c)
                        player_shuffle = randperm(length(cards),1);
                        player_1_cards(p) = cards_array(cards(player_shuffle));
                        cards(player_shuffle) = [];
                                               
                        array1(1,p+4) = player_1_cards(p);                
                        board_display(r,c) = red_sprite;
                        
                        %checking if the player won
                        winner = winner_check(r,c,board_display,red_sprite,corner,array1); 
                        if winner == 1
                            fprintf("player 1 won\n")
                            player_turn = 0;
                            board_display(1,4) = star;
                            board_display(1,11) = star;
                        end
                        break
                    else %if the player does not have the cards that they selected on the board
                        no_card = no_card + 1;
                        
                        
                    end
                                  
                end
                
            %if the player selects the red king button at the left side of the board   
            elseif (r == 7 && c == 1) || (r == 8 && c == 1)
                    for i = 1:6
                        %if the player has a red king
                        if player_1_cards(i) == red_kings(1) || player_1_cards(i) == red_kings(2)                    
                            fprintf("place your chip\n")
                            %placing the chip where the player wants and updating the main deck
                            [row,col] = getMouseInput(my_scene); 
                            index = find(cards_array == array1(row,col));
                            card_index = find(cards == index);
                            cards(card_index(1)) = [];
                            player_shuffle = cards(randperm(length(cards),1));
                            player_1_cards(i) = cards_array(cards(player_shuffle));                        
                            cards(player_shuffle) = [];
                            array1(1,i+4) = player_1_cards(i);
                                                    
                            board_display(row,col) = red_sprite;
                            winner = winner_check(row,col,board_display,red_sprite,corner,array1);
                            %checking if the player won
                            if winner == 1
                                fprintf("player 1 won\n")
                                player_turn = 0;
                                board_display(1,4) = star;
                                board_display(1,11) = star;
                            end
                            break
                        else
                            %if the player does not have a red king
                            no_red_king = no_red_king + 1;                       
                        end
                    end
            %if the player selects the black king button at the left side of the board     
            elseif (r == 7 && c == 14) || (r == 8 && c == 14)
                %loop to check all 6 cards that the player has
                for i = 1:6
                    
                    if player_1_cards(i) == black_kings(1) || player_1_cards(i) == black_kings(2)                    
                        fprintf("choose your chip to take away\n")
                        [row,col] = getMouseInput(my_scene);
                        check = 0;
                        while check == 0
                            %removing the chip where the player wants and updating the main deck
                            if board_display(row,col) == red_sprite || board_display(row,col) == blue_sprite
                                index = find(cards_array == array1(row,col));
                                cards(end+1) = index;
                                player_shuffle = randperm(length(cards),1);
                                player_1_cards(i) = cards_array(cards(player_shuffle));
                                cards(player_shuffle) = [];
                                
                                array1(1,i+4) = player_1_cards(i); 
                                
                             
                                board_display(row,col) = empty_sprite;
                                winner = winner_check(row,col,board_display,red_sprite,corner,array1);
                                %checking if the player won
                                if winner == 1
                                    fprintf("player 1 won\n")
                                    
                                    player_turn = 0;
                                    board_display(1,4) = star;
                                    board_display(1,11) = star;
                                end
                                check = 1;
                                
                            else %if the spot selected does not have a chip
                                fprintf("choose a place with a chip on it\n")
                            end
                        end
                        break
                        
                    else %if the player did not have a black king 
                        no_black_king = no_black_king + 1;
                        
                    end
                end
                
            end
            %printing the results 
            if no_red_king == 6
                fprintf("you dont have a red king\n")
            elseif no_black_king == 6
                fprintf("you dont have a black king\n")
            elseif no_card == 6
                fprintf("you dont have the card\n")
            elseif winner == 1
    
                player_turn = 0;
            elseif no_chip == 1
                player_turn = 1;
            else 
                player_turn = 2;
            end
            player_turn
            drawScene(my_scene,board_display,array1);
            
    
        end
        %starting second player's turn
        while player_turn == 2
            [r,c] = getMouseInput(my_scene);
            no_card = 0;
            no_red_king = 0;
            no_black_king = 0;
            no_chip = 0;
            %checking if the place selected has a chip already on it
            if board_display(r,c) == blue_sprite
                fprintf("place the chip on an empty space\n")
                no_chip = 1;
            %playing a turn without using any kings
            elseif board_display(r,c) ~= blue_sprite && (array1(r,c) ~= red_kings(1) && array1(r,c) ~= red_kings(2) && array1(r,c) ~= black_kings(1) && array1(r,c) ~= black_kings(2))
                %loop to check all 6 cards that the player has
                for p = 1:6 
                    %comparing selected spot on the boards and cards in the player's deck and updating the main deck  
                    if player_2_cards(p) == array1(r,c) 
                        player_shuffle = randperm(length(cards),1);
                        player_2_cards(p) = cards_array(cards(player_shuffle));
                        cards(player_shuffle) = [];
                        
                       
                        array1(14,p+4) = player_2_cards(p);                
                        board_display(r,c) = blue_sprite;
                        %checking if the player won
                        winner = winner_check(r,c,board_display,blue_sprite,corner,array1);
                        
                        if winner == 1
                            fprintf("player 2 won\n")
                            player_turn = 0;
                            board_display(14,4) = star;
                            board_display(14,11) = star;
                        end
                        break
                    else %if the player does not have the cards that they selected on the board
                        no_card = no_card + 1;
                        
                        
                    end              
                end
        
            %if the player selects the red king button at the left side of the board     
            elseif (r == 7 && c == 1) || (r == 8 && c == 1)
                    for i = 1:6
                        %if the player has a red king
                        if player_2_cards(i) == red_kings(1) || player_2_cards(i) == red_kings(2)                    
                            fprintf("place your chip\n")
                            %placing the chip where the player wants and updating the main deck
                            [row,col] = getMouseInput(my_scene);
                            index = find(cards_array == array1(row,col));
                            card_index = find(cards == index);
                            cards(card_index(1)) = [];
                            player_shuffle = randperm(length(cards),1);
                            player_2_cards(i) = cards_array(cards(player_shuffle));
                            cards(player_shuffle) = [];
                            
                            array1(14,i+4) = player_2_cards(i);
                            
                            
                            board_display(row,col) = blue_sprite;
                            %checking if the player won
                            winner = winner_check(row,col,board_display,blue_sprite,corner,array1);
                            if winner == 1
                                fprintf("player 2 won\n")
                                player_turn = 0;
                                board_display(14,4) = star;
                                board_display(14,11) = star;
                            end
                            break
                            
                        else
                            %if the player does not have a red king 
                            no_red_king = no_red_king + 1;
                            
                        end
                    end
            %if the player selects the black king button at the left side of the board    
            elseif (r == 7 && c == 14) || (r == 8 && c == 14)
                %loop to check all 6 cards that the player has
                for i = 1:6
                    if player_2_cards(i) == black_kings(1) || player_2_cards(i) == black_kings(2)                    
                        fprintf("choose your chip to take away\n")
                        %removing the chip where the player wants and updating the main deck
                        [row,col] = getMouseInput(my_scene);
                        check = 0;
                        while check == 0
                            if board_display(row,col) == blue_sprite || board_display(row,col) == red_sprite
                                index = find(cards_array == array1(row,col));
                                cards(end+1) = index;
                                player_shuffle = randperm(length(cards),1);
                                player_2_cards(i) = cards_array(cards(player_shuffle));
                                cards(player_shuffle) = [];
                                
                                array1(14,i+4) = player_2_cards(i); 
                                
                             
                                board_display(row,col) = empty_sprite;
                                %checking if the player won
                                winner = winner_check(row,col,board_display,blue_sprite,corner,array1);
                                if winner == 1
                                    fprintf("player 2 won\n")
                                    player_turn = 0;
                                    board_display(14,4) = star;
                                    board_display(14,11) = star;
                                end
                                check = 1;
                                
                            else %if the spot selected does not have a chip
                                fprintf("choose a place with a chip on it\n")
                            end
                        end
                        break
                        
                    else %if the player does not have a black king
                        no_black_king = no_black_king + 1;
                        
                    end
                end
                
            end
            %printing the results 
            if no_red_king == 6
                fprintf("you dont have a red king\n")
            elseif no_black_king == 6
                fprintf("you dont have a black king\n")
            elseif no_card == 6
                fprintf("you dont have the card\n")
            elseif winner == 1
                player_turn = 0;
            elseif no_chip == 1
                player_turn = 2;
            else
                player_turn = 1;
            end
            player_turn
            drawScene(my_scene,board_display,array1);
            
        end   
       
    end
    replay = input("Do you wanna play again ? (y/n) ","s");
    loop = 0;
    while loop == 0

        if replay == "n"
            play_again = 0;
            loop = 1;
        elseif replay == "y"
            play_again = 1;
            loop = 1;
        else
            replay = input("enter a valid answer ","s");
        end
    end
end




