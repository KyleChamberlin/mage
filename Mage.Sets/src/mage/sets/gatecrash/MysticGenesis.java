/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.sets.gatecrash;

import java.util.UUID;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Rarity;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.CardImpl;
import mage.game.Game;
import mage.game.permanent.token.Token;
import mage.game.stack.StackObject;
import mage.target.TargetSpell;

/**
 *
 * @author LevelX2
 */
public class MysticGenesis extends CardImpl {

    public MysticGenesis(UUID ownerId) {
        super(ownerId, 180, "Mystic Genesis", Rarity.RARE, new CardType[]{CardType.INSTANT}, "{2}{G}{U}{U}");
        this.expansionSetCode = "GTC";

        // Counter target spell. Put an X/X green Ooze creature token onto the battlefield, where X is that spell's converted mana cost.
        this.getSpellAbility().addTarget(new TargetSpell());
        this.getSpellAbility().addEffect(new MysticGenesisEffect());

    }

    public MysticGenesis(final MysticGenesis card) {
        super(card);
    }

    @Override
    public MysticGenesis copy() {
        return new MysticGenesis(this);
    }
}

class MysticGenesisEffect extends OneShotEffect {

    public MysticGenesisEffect() {
        super(Outcome.Detriment);
        staticText = "Counter target spell. Put an X/X green Ooze creature token onto the battlefield, where X is that spell's converted mana cost";
    }

    public MysticGenesisEffect(final MysticGenesisEffect effect) {
        super(effect);
    }

    @Override
    public MysticGenesisEffect copy() {
        return new MysticGenesisEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        StackObject stackObject = game.getStack().getStackObject(targetPointer.getFirst(game, source));
        if (stackObject != null) {
            game.getStack().counter(source.getFirstTarget(), source.getSourceId(), game);
            return new CreateTokenEffect(new MysticGenesisOozeToken(stackObject.getConvertedManaCost())).apply(game, source);
        }
        return false;
    }
}

class MysticGenesisOozeToken extends Token {
    
    public MysticGenesisOozeToken(int xValue) {
        super("Ooze", "X/X green Ooze creature token");
        cardType.add(CardType.CREATURE);
        color.setGreen(true);
        subtype.add("Ooze");
        power = new MageInt(xValue);
        toughness = new MageInt(xValue);
        setOriginalExpansionSetCode("RTR");
    }
}
