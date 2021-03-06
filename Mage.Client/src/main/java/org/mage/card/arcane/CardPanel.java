package org.mage.card.arcane;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import mage.cards.MagePermanent;
import mage.cards.TextPopup;
import mage.cards.action.ActionCallback;
import mage.cards.action.TransferData;
import mage.client.dialog.PreferencesDialog;
import mage.client.plugins.adapters.MageActionCallback;
import mage.client.plugins.impl.Plugins;
import mage.client.util.ImageCaches;
import mage.client.util.ImageHelper;
import mage.client.util.audio.AudioManager;
import mage.components.ImagePanel;
import mage.constants.AbilityType;
import mage.constants.CardType;
import mage.constants.EnlargeMode;
import mage.utils.CardUtil;
import mage.view.AbilityView;
import mage.view.CardView;
import mage.view.CounterView;
import mage.view.PermanentView;
import mage.view.StackAbilityView;
import net.java.truevfs.access.TFile;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.graphics.GraphicsUtilities;
import static org.mage.plugins.card.constants.Constants.THUMBNAIL_SIZE_FULL;
import org.mage.plugins.card.dl.sources.DirectLinksForDownload;
import org.mage.plugins.card.images.ImageCache;
import org.mage.plugins.card.utils.impl.ImageManagerImpl;

/**
 * Main class for drawing Mage card object.
 *
 * @author arcane, nantuko, noxx
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CardPanel extends MagePermanent implements MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener {

    private static final long serialVersionUID = -3272134219262184410L;

    private static final Logger LOGGER = Logger.getLogger(CardPanel.class);

    private static final int WIDTH_LIMIT = 90; // card width limit to create smaller counter
    public static final double TAPPED_ANGLE = Math.PI / 2;
    public static final double FLIPPED_ANGLE = Math.PI;
    public static final float ASPECT_RATIO = 3.5f / 2.5f;
    public static final int POPUP_X_GAP = 1; // prevent tooltip window from blinking

    public static CardPanel dragAnimationPanel;

    public static final Rectangle CARD_SIZE_FULL = new Rectangle(101, 149);

    private static final float ROUNDED_CORNER_SIZE = 0.1f;
    private static final float BLACK_BORDER_SIZE = 0.03f;
    private static final int TEXT_GLOW_SIZE = 6;
    private static final float TEXT_GLOW_INTENSITY = 3f;
    private static final float ROT_CENTER_TO_TOP_CORNER = 1.0295630140987000315797369464196f;
    private static final float ROT_CENTER_TO_BOTTOM_CORNER = 0.7071067811865475244008443621048f;

    public CardView gameCard;
    public CardView updateCard;

    // for two faced cards
    public CardView temporary;
    private List<MagePermanent> links = new ArrayList<>();

    public double tappedAngle = 0;
    public double flippedAngle = 0;
    public final ScaledImagePanel imagePanel;
    public ImagePanel overlayPanel;

    public JPanel buttonPanel;
    private JButton dayNightButton;

    public JPanel copyIconPanel;
    private JButton showCopySourceButton;

    public JPanel iconPanel;
    private JButton typeButton;

    public JPanel counterPanel;
    private JLabel loyaltyCounterLabel;
    private JLabel plusCounterLabel;
    private JLabel otherCounterLabel;
    private JLabel minusCounterLabel;
    private int loyaltyCounter;
    private int plusCounter;
    private int otherCounter;
    private int minusCounter;
    private int lastCardWidth;

    private GlowText titleText;
    private GlowText ptText;
    private boolean displayEnabled = true;
    private boolean isAnimationPanel;
    public int cardXOffset, cardYOffset, cardWidth, cardHeight;
    private int symbolWidth;

    private boolean isSelected;
    private boolean isPlayable;
    private boolean isChoosable;
    private boolean canAttack;
    private boolean showCastingCost;
    private boolean hasImage = false;
    private float alpha = 1.0f;

    private ActionCallback callback;

    protected boolean tooltipShowing;
    protected TextPopup tooltipText = new TextPopup();
    protected UUID gameId;
    private TransferData data = new TransferData();

    private boolean isPermanent;
    private boolean hasSickness;
    private String zone;

    public double transformAngle = 1;

    private boolean transformed;
    private boolean animationInProgress = false;

    private boolean displayTitleAnyway;

    private JPanel cardArea;

    private int yTextOffset = 10;

    // if this is set, it's opened if the user right clicks on the card panel
    private JPopupMenu popupMenu;

    private static Map<Key, BufferedImage> IMAGE_CACHE;

    private final static class Key
    {
        final int width;
        final int height;
        final int cardWidth;
        final int cardHeight;
        final int cardXOffset;
        final int cardYOffset;
        final boolean hasImage;
        final boolean isSelected;
        final boolean isChoosable;
        final boolean isPlayable;
        final boolean canAttack;

        public Key(int width, int height, int cardWidth, int cardHeight, int cardXOffset, int cardYOffset, boolean hasImage, boolean isSelected, boolean isChoosable, boolean isPlayable, boolean canAttack) {
            this.width = width;
            this.height = height;
            this.cardWidth = cardWidth;
            this.cardHeight = cardHeight;
            this.cardXOffset = cardXOffset;
            this.cardYOffset = cardYOffset;
            this.hasImage = hasImage;
            this.isSelected = isSelected;
            this.isChoosable = isChoosable;
            this.isPlayable = isPlayable;
            this.canAttack = canAttack;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 19 * hash + this.width;
            hash = 19 * hash + this.height;
            hash = 19 * hash + this.cardWidth;
            hash = 19 * hash + this.cardHeight;
            hash = 19 * hash + this.cardXOffset;
            hash = 19 * hash + this.cardYOffset;
            hash = 19 * hash + (this.hasImage ? 1 : 0);
            hash = 19 * hash + (this.isSelected ? 1 : 0);
            hash = 19 * hash + (this.isChoosable ? 1 : 0);
            hash = 19 * hash + (this.isPlayable ? 1 : 0);
            hash = 19 * hash + (this.canAttack ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if (this.width != other.width) {
                return false;
            }
            if (this.height != other.height) {
                return false;
            }
            if (this.cardWidth != other.cardWidth) {
                return false;
            }
            if (this.cardHeight != other.cardHeight) {
                return false;
            }
            if (this.cardXOffset != other.cardXOffset) {
                return false;
            }
            if (this.cardYOffset != other.cardYOffset) {
                return false;
            }
            if (this.hasImage != other.hasImage) {
                return false;
            }
            if (this.isSelected != other.isSelected) {
                return false;
            }
            if (this.isChoosable != other.isChoosable) {
                return false;
            }
            if (this.isPlayable != other.isPlayable) {
                return false;
            }
            if (this.canAttack != other.canAttack) {
                return false;
            }
            return true;
        }
    }

    static {
        IMAGE_CACHE = ImageCaches.register(new MapMaker().softValues().makeComputingMap(new Function<Key, BufferedImage>() {
            @Override
            public BufferedImage apply(Key key) {
                return createImage(key);
            }
        }));
    }

    public CardPanel(CardView newGameCard, UUID gameId, final boolean loadImage, ActionCallback callback, final boolean foil, Dimension dimension) {
        this.gameCard = newGameCard;
        this.callback = callback;
        this.gameId = gameId;

        this.isPermanent = this.gameCard instanceof PermanentView;
        if (isPermanent) {
            this.hasSickness = ((PermanentView) this.gameCard).hasSummoningSickness();
        }

        this.setCardBounds(0, 0, dimension.width, dimension.height);

        //for container debug (don't remove)
        //setBorder(BorderFactory.createLineBorder(Color.green));
        if (this.gameCard.canTransform()) {
            buttonPanel = new JPanel();
            buttonPanel.setLayout(null);
            buttonPanel.setOpaque(false);
            add(buttonPanel);

            dayNightButton = new JButton("");
            dayNightButton.setLocation(2, 2);
            dayNightButton.setSize(25, 25);

            buttonPanel.setVisible(true);

            BufferedImage day = ImageManagerImpl.getInstance().getDayImage();
            dayNightButton.setIcon(new ImageIcon(day));

            buttonPanel.add(dayNightButton);

            dayNightButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // if card is being rotated, ignore action performed
                    // if card is tapped, no visual transforming is possible (implementation limitation)
                    // if card is permanent, it will be rotated by Mage, so manual rotate should be possible
                    if (animationInProgress || isTapped() || isPermanent) {
                        return;
                    }
                    Animation.transformCard(CardPanel.this, CardPanel.this, true);
                }
            });
        }
        if (!newGameCard.isAbility()) {
            // panel to show counters on the card
            counterPanel = new JPanel();
            counterPanel.setLayout(null);
            counterPanel.setOpaque(false);
            add(counterPanel);

            plusCounterLabel = new JLabel("");
            plusCounterLabel.setToolTipText("+1/+1");
            counterPanel.add(plusCounterLabel);

            minusCounterLabel = new JLabel("");
            minusCounterLabel.setToolTipText("-1/-1");
            counterPanel.add(minusCounterLabel);

            loyaltyCounterLabel = new JLabel("");
            loyaltyCounterLabel.setToolTipText("loyalty");
            counterPanel.add(loyaltyCounterLabel);

            otherCounterLabel = new JLabel("");
            counterPanel.add(otherCounterLabel);

            counterPanel.setVisible(false);
        }
        if (newGameCard.isAbility()) {
            if (AbilityType.TRIGGERED.equals(newGameCard.getAbilityType())) {
                setTypeIcon(ImageManagerImpl.getInstance().getTriggeredAbilityImage(), "Triggered Ability");
            } else if (AbilityType.ACTIVATED.equals(newGameCard.getAbilityType())) {
                setTypeIcon(ImageManagerImpl.getInstance().getActivatedAbilityImage(), "Activated Ability");
            }
        }

        if (this.gameCard.isToken()) {
            setTypeIcon(ImageManagerImpl.getInstance().getTokenIconImage(), "Token Permanent");
        }

        // icon to inform about permanent is copying something
        if (this.gameCard instanceof PermanentView) {
            copyIconPanel = new JPanel();
            copyIconPanel.setLayout(null);
            copyIconPanel.setOpaque(false);
            add(copyIconPanel);

            showCopySourceButton = new JButton("");
            showCopySourceButton.setLocation(2, 2);
            showCopySourceButton.setSize(25, 25);
            showCopySourceButton.setToolTipText("This permanent is copying a target. To see original image, push this button or turn mouse wheel down while hovering with the mouse pointer over the permanent.");
            copyIconPanel.setVisible(((PermanentView) this.gameCard).isCopy());

            showCopySourceButton.setIcon(new ImageIcon(ImageManagerImpl.getInstance().getCopyInformIconImage()));

            copyIconPanel.add(showCopySourceButton);

            showCopySourceButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ActionCallback callback = Plugins.getInstance().getActionCallback();
                    ((MageActionCallback) callback).enlargeCard(EnlargeMode.COPY);
                }
            });
        }

        setBackground(Color.black);
        setOpaque(false);

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addComponentListener(this);

        displayTitleAnyway = PreferencesDialog.getCachedValue(PreferencesDialog.KEY_SHOW_CARD_NAMES, "true").equals("true");

        titleText = new GlowText();
        setText(gameCard);
//        int fontSize = (int) cardHeight / 11;
//        titleText.setFont(getFont().deriveFont(Font.BOLD, fontSize));
        titleText.setForeground(Color.white);
        titleText.setGlow(Color.black, TEXT_GLOW_SIZE, TEXT_GLOW_INTENSITY);
        titleText.setWrap(true);
        add(titleText);

        ptText = new GlowText();
        if (CardUtil.isCreature(gameCard)) {
            ptText.setText(gameCard.getPower() + "/" + gameCard.getToughness());
        } else if (CardUtil.isPlaneswalker(gameCard)) {
            ptText.setText(gameCard.getLoyalty());
        }
//        ptText.setFont(getFont().deriveFont(Font.BOLD, fontSize));
        ptText.setForeground(Color.white);
        ptText.setGlow(Color.black, TEXT_GLOW_SIZE, TEXT_GLOW_INTENSITY);
        add(ptText);

        BufferedImage sickness = ImageManagerImpl.getInstance().getSicknessImage();
        overlayPanel = new ImagePanel(sickness, ImagePanel.SCALED);
        overlayPanel.setOpaque(false);
        add(overlayPanel);

        imagePanel = new ScaledImagePanel();
        imagePanel.setBorder(BorderFactory.createLineBorder(Color.white));
        add(imagePanel);

        String cardType = getType(newGameCard);
        tooltipText.setText(getText(cardType, newGameCard));

        tappedAngle = isTapped() ? CardPanel.TAPPED_ANGLE : 0;
        flippedAngle = isFlipped() ? CardPanel.FLIPPED_ANGLE : 0;

        if (!loadImage) {
            return;
        }

        if (gameCard.isTransformed()) {
            // this calls updateImage
            toggleTransformed();
        } else {
            updateImage();
        }
    }

    private void setTypeIcon(BufferedImage bufferedImage, String toolTipText) {
        iconPanel = new JPanel();
        iconPanel.setLayout(null);
        iconPanel.setOpaque(false);
        add(iconPanel);

        typeButton = new JButton("");
        typeButton.setLocation(2, 2);
        typeButton.setSize(25, 25);

        iconPanel.setVisible(true);
        typeButton.setIcon(new ImageIcon(bufferedImage));
        if (toolTipText != null) {
            typeButton.setToolTipText(toolTipText);
        }
        iconPanel.add(typeButton);
    }

    public void cleanUp() {
        if (dayNightButton != null) {
            for (ActionListener al : dayNightButton.getActionListeners()) {
                dayNightButton.removeActionListener(al);
            }
        }
        for (MouseListener ml : this.getMouseListeners()) {
            this.removeMouseListener(ml);
        }
        for (MouseMotionListener ml : this.getMouseMotionListeners()) {
            this.removeMouseMotionListener(ml);
        }
        for (MouseWheelListener ml : this.getMouseWheelListeners()) {
            this.removeMouseWheelListener(ml);
        }
        // this holds reference to ActionCallback forever so set it to null to prevent
        this.callback = null;
        this.data = null;
        this.counterPanel = null;
    }

    private void setText(CardView card) {
        titleText.setText(!displayTitleAnyway && hasImage ? "" : card.getName());
    }

    private void setImage(BufferedImage srcImage) {
        synchronized (imagePanel) {
            if(srcImage != null)
                imagePanel.setImage(srcImage);
            else
                imagePanel.clearImage();
            repaint();
        }
        doLayout();
    }

    public void setImage(final CardPanel panel) {
        synchronized (panel.imagePanel) {
            if (panel.imagePanel.hasImage()) {
                setImage(panel.imagePanel.getSrcImage());
            }
        }
    }

    @Override
    public void setZone(String zone) {
        this.zone = zone;
    }

    @Override
    public String getZone() {
        return zone;
    }

    public void setDisplayEnabled(boolean displayEnabled) {
        this.displayEnabled = displayEnabled;
    }

    public boolean isDisplayEnabled() {
        return displayEnabled;
    }

    public void setAnimationPanel(boolean isAnimationPanel) {
        this.isAnimationPanel = isAnimationPanel;
    }

    @Override
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        if (isSelected) {
            this.titleText.setGlowColor(Color.green);
        } else {
            this.titleText.setGlowColor(Color.black);
        }
        // noxx: bad idea is to call repaint in setter method
        ////repaint();
    }

    @Override
    public void setChoosable(boolean isChoosable) {
        this.isChoosable = isChoosable;
    }

    @Override
    public void setCardAreaRef(JPanel cardArea) {
        this.cardArea = cardArea;
    }

    public boolean getSelected() {
        return this.isSelected;
    }

    public void setShowCastingCost(boolean showCastingCost) {
        this.showCastingCost = showCastingCost;
    }

    @Override
    public void paint(Graphics g) {
        if (!displayEnabled) {
            return;
        }
        if (!isValid()) {
            super.validate();
        }
        Graphics2D g2d = (Graphics2D) g;
        if (transformAngle < 1) {
            float edgeOffset = (cardWidth + cardXOffset) / 2f;
            g2d.translate(edgeOffset * (1 - transformAngle), 0);
            g2d.scale(transformAngle, 1);
        }
        if (tappedAngle + flippedAngle > 0) {
            g2d = (Graphics2D) g2d.create();
            float edgeOffset = cardWidth / 2f;
            double angle = tappedAngle + (Math.abs(flippedAngle - FLIPPED_ANGLE) < 0.001 ? 0 : flippedAngle);
            g2d.rotate(angle, cardXOffset + edgeOffset, cardYOffset + cardHeight - edgeOffset);
        }
        super.paint(g2d);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)(g.create());

        if (alpha != 1.0f) {
            AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha);
            g2d.setComposite(composite);
        }

        g2d.drawImage(IMAGE_CACHE.get(new Key(getWidth(), getHeight(), cardWidth, cardHeight, cardXOffset, cardYOffset, hasImage, isSelected, isChoosable, isPlayable, canAttack)), 0, 0, null);
        g2d.dispose();
    }

    private static BufferedImage createImage(Key key) {
        int cardWidth = key.cardWidth;
        int cardHeight = key.cardHeight;
        int cardXOffset = key.cardXOffset;
        int cardYOffset = key.cardYOffset;

        BufferedImage image = GraphicsUtilities.createCompatibleTranslucentImage(key.width, key.height);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!key.hasImage) {
            g2d.setColor(new Color(30, 200, 200, 120));
        } else {
            g2d.setColor(new Color(0, 0, 0, 0));
        }

        int cornerSize = Math.max(4, Math.round(cardWidth * ROUNDED_CORNER_SIZE));
        g2d.fillRoundRect(cardXOffset, cardYOffset, cardWidth, cardHeight, cornerSize, cornerSize);

        if (key.isSelected) {
            g2d.setColor(Color.green);
            g2d.fillRoundRect(cardXOffset + 1, cardYOffset + 1, cardWidth - 2, cardHeight - 2, cornerSize, cornerSize);
        } else if (key.isChoosable) {
            g2d.setColor(new Color(250, 250, 0, 230));
            g2d.fillRoundRect(cardXOffset + 1, cardYOffset + 1, cardWidth - 2, cardHeight - 2, cornerSize, cornerSize);
        } else if (key.isPlayable) {
            g2d.setColor(new Color(153, 102, 204, 200));
            //g2d.fillRoundRect(cardXOffset + 1, cardYOffset + 1, cardWidth - 2, cardHeight - 2, cornerSize, cornerSize);
            g2d.fillRoundRect(cardXOffset, cardYOffset, cardWidth, cardHeight, cornerSize, cornerSize);
        }

        if (key.canAttack) {
            g2d.setColor(new Color(0, 0, 255, 230));
            g2d.fillRoundRect(cardXOffset + 1, cardYOffset + 1, cardWidth - 2, cardHeight - 2, cornerSize, cornerSize);
        }

        //TODO:uncomment
        /*
         if (gameCard.isAttacking()) {
         g2d.setColor(new Color(200,10,10,200));
         g2d.fillRoundRect(cardXOffset+1, cardYOffset+1, cardWidth-2, cardHeight-2, cornerSize, cornerSize);
         }*/
        g2d.dispose();

        return image;
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        if (showCastingCost && !isAnimationPanel && cardWidth < 200 && cardWidth > 60) {
            String manaCost = ManaSymbols.getStringManaCost(gameCard.getManaCost());
            int width = getWidth(manaCost);
            if (hasImage) {
                ManaSymbols.draw(g, manaCost, cardXOffset + cardWidth - width - 5, cardYOffset + 5, symbolWidth);
            } else {
                ManaSymbols.draw(g, manaCost, cardXOffset + 8, cardHeight - 9, symbolWidth);
            }
        }
    }

    private int getWidth(String manaCost) {
        int width = 0;
        manaCost = manaCost.replace("\\", "");
        StringTokenizer tok = new StringTokenizer(manaCost, " ");
        while (tok.hasMoreTokens()) {
            tok.nextToken();
            width += symbolWidth;
        }
        return width;
    }

    @Override
    public void doLayout() {
        int borderSize = Math.round(cardWidth * BLACK_BORDER_SIZE);
        imagePanel.setLocation(cardXOffset + borderSize, cardYOffset + borderSize);
        imagePanel.setSize(cardWidth - borderSize * 2, cardHeight - borderSize * 2);

        if (hasSickness && CardUtil.isCreature(gameCard) && isPermanent) {
            overlayPanel.setLocation(cardXOffset + borderSize, cardYOffset + borderSize);
            overlayPanel.setSize(cardWidth - borderSize * 2, cardHeight - borderSize * 2);
        } else {
            overlayPanel.setVisible(false);
        }

        if (buttonPanel != null) {
            buttonPanel.setLocation(cardXOffset + borderSize, cardYOffset + borderSize);
            buttonPanel.setSize(cardWidth - borderSize * 2, cardHeight - borderSize * 2);
            dayNightButton.setLocation(0, cardHeight - 30);
        }
        if (iconPanel != null) {
            iconPanel.setLocation(cardXOffset + borderSize, cardYOffset + borderSize);
            iconPanel.setSize(cardWidth - borderSize * 2, cardHeight - borderSize * 2);
        }
        if (copyIconPanel != null) {
            copyIconPanel.setLocation(cardXOffset + borderSize, cardYOffset + borderSize);
            copyIconPanel.setSize(cardWidth - borderSize * 2, cardHeight - borderSize * 2);
        }
        if (counterPanel != null) {
            counterPanel.setLocation(cardXOffset + borderSize, cardYOffset + borderSize);
            counterPanel.setSize(cardWidth - borderSize * 2, cardHeight - borderSize * 2);
            int size = cardWidth > WIDTH_LIMIT ? 40 : 20;

            minusCounterLabel.setLocation(counterPanel.getWidth() - size, counterPanel.getHeight() - size * 2);
            minusCounterLabel.setSize(size, size);

            plusCounterLabel.setLocation(5, counterPanel.getHeight() - size * 2);
            plusCounterLabel.setSize(size, size);

            loyaltyCounterLabel.setLocation(counterPanel.getWidth() - size, counterPanel.getHeight() - size);
            loyaltyCounterLabel.setSize(size, size);

            otherCounterLabel.setLocation(5, counterPanel.getHeight() - size);
            otherCounterLabel.setSize(size, size);

        }
        int fontHeight = Math.round(cardHeight * (27f / 680));
        boolean showText = (!isAnimationPanel && fontHeight < 12);
        titleText.setVisible(showText);
        ptText.setVisible(showText);

        if (showText) {
            int fontSize = (int) cardHeight / 11;
            titleText.setFont(getFont().deriveFont(Font.BOLD, fontSize));

            int titleX = Math.round(cardWidth * (20f / 480));
            int titleY = Math.round(cardHeight * (9f / 680)) + yTextOffset;
            titleText.setBounds(cardXOffset + titleX, cardYOffset + titleY, cardWidth - titleX, cardHeight - titleY);

            ptText.setFont(getFont().deriveFont(Font.BOLD, fontSize));
            Dimension ptSize = ptText.getPreferredSize();
            ptText.setSize(ptSize.width, ptSize.height);
            int ptX = Math.round(cardWidth * (420f / 480)) - ptSize.width / 2;
            int ptY = Math.round(cardHeight * (675f / 680)) - ptSize.height;

            int offsetX = Math.round((CARD_SIZE_FULL.width - cardWidth) / 10.0f);

            ptText.setLocation(cardXOffset + ptX - TEXT_GLOW_SIZE / 2 - offsetX, cardYOffset + ptY - TEXT_GLOW_SIZE / 2);
        }
    }

    @Override
    public String toString() {
        return gameCard.toString();
    }

    @Override
    public final void setCardBounds(int x, int y, int cardWidth, int cardHeight) {
        if(cardWidth == this.cardWidth && cardHeight == this.cardHeight) {
            setBounds(x - cardXOffset, y - cardYOffset, getWidth(), getHeight());
            return;
        }

        this.cardWidth = cardWidth;
        this.symbolWidth = cardWidth / 7;
        this.cardHeight = cardHeight;
        if (this.isPermanent) {
            int rotCenterX = Math.round(cardWidth / 2f);
            int rotCenterY = cardHeight - rotCenterX;
            int rotCenterToTopCorner = Math.round(cardWidth * CardPanel.ROT_CENTER_TO_TOP_CORNER);
            int rotCenterToBottomCorner = Math.round(cardWidth * CardPanel.ROT_CENTER_TO_BOTTOM_CORNER);
            int xOffset = getXOffset(cardWidth);
            int yOffset = getYOffset(cardWidth, cardHeight);
            cardXOffset = -xOffset;
            cardYOffset = -yOffset;
            int width = -xOffset + rotCenterX + rotCenterToTopCorner;
            int height = -yOffset + rotCenterY + rotCenterToBottomCorner;
            setBounds(x + xOffset, y + yOffset, width, height);
        } else {
            cardXOffset = 5;
            cardYOffset = 5;
            int width = cardXOffset * 2 + cardWidth;
            int height = cardYOffset * 2 + cardHeight;
            setBounds(x - cardXOffset, y - cardYOffset, width, height);
        }
        if(imagePanel != null && imagePanel.getSrcImage() != null)
            updateImage();
    }

    public int getXOffset(int cardWidth) {
        if (this.isPermanent) {
            int rotCenterX = Math.round(cardWidth / 2f);
            int rotCenterToBottomCorner = Math.round(cardWidth * CardPanel.ROT_CENTER_TO_BOTTOM_CORNER);
            int xOffset = rotCenterX - rotCenterToBottomCorner;
            return xOffset;
        } else {
            return cardXOffset;
        }
    }

    public int getYOffset(int cardWidth, int cardHeight) {
        if (this.isPermanent) {
            int rotCenterX = Math.round(cardWidth / 2f);
            int rotCenterY = cardHeight - rotCenterX;
            int rotCenterToTopCorner = Math.round(cardWidth * CardPanel.ROT_CENTER_TO_TOP_CORNER);
            int yOffset = rotCenterY - rotCenterToTopCorner;
            return yOffset;
        } else {
            return cardYOffset;
        }

    }

    public int getCardX() {
        return getX() + cardXOffset;
    }

    public int getCardY() {
        return getY() + cardYOffset;
    }

    public int getCardWidth() {
        return cardWidth;
    }

    public int getCardHeight() {
        return cardHeight;
    }

    public Point getCardLocation() {
        Point p = getLocation();
        p.x += cardXOffset;
        p.y += cardYOffset;
        return p;
    }

    public CardView getCard() {
        return this.gameCard;
    }

    @Override
    public void setAlpha(float alpha) {
        this.alpha = alpha;
        if (alpha == 0) {
            this.ptText.setVisible(false);
            this.titleText.setVisible(false);
        } else if (alpha == 1.0f) {
            this.ptText.setVisible(true);
            this.titleText.setVisible(true);
        }
    }

    @Override
    public float getAlpha() {
        return alpha;
    }

    public int getCardXOffset() {
        return cardXOffset;
    }

    public int getCardYOffset() {
        return cardYOffset;
    }

    private int updateImageStamp;

    @Override
    public void updateImage() {
        tappedAngle = isTapped() ? CardPanel.TAPPED_ANGLE : 0;
        flippedAngle = isFlipped() ? CardPanel.FLIPPED_ANGLE : 0;

        final CardView gameCard = this.gameCard;
        final int stamp = ++updateImageStamp;

        Util.threadPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final BufferedImage srcImage;
                    if (gameCard.isFaceDown()) {
                        srcImage = getFaceDownImage();
                    } else if (cardWidth > THUMBNAIL_SIZE_FULL.width) {
                        srcImage = ImageCache.getImage(gameCard, cardWidth, cardHeight);
                    } else {
                        srcImage = ImageCache.getThumbnail(gameCard);
                    }
                    UI.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            if(stamp == updateImageStamp) {
                                hasImage = srcImage != null;
                                setText(gameCard);
                                setImage(srcImage);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (Error err) {
                    err.printStackTrace();
                }
            }
        });
    }

    private BufferedImage getFaceDownImage() {
        if (isPermanent) {
            if (((PermanentView) gameCard).isMorphed()) {
                return ImageCache.getMorphImage();
            } else {
                return ImageCache.getManifestImage();
            }
        } else if (this.gameCard instanceof StackAbilityView) {
            return ImageCache.getMorphImage();
        } else {
            return ImageCache.loadImage(new TFile(DirectLinksForDownload.outDir + File.separator + DirectLinksForDownload.cardbackFilename));
        }
    }

    @Override
    public List<MagePermanent> getLinks() {
        return links;
    }

    @Override
    public boolean isTapped() {
        if (isPermanent) {
            return ((PermanentView) gameCard).isTapped();
        }
        return false;
    }

    @Override
    public boolean isFlipped() {
        if (isPermanent) {
            return ((PermanentView) gameCard).isFlipped();
        }
        return false;
    }

    @Override
    public boolean isTransformed() {
        if (isPermanent) {
            return gameCard.isTransformed();
        }
        return false;
    }

    @Override
    public void showCardTitle() {
        displayTitleAnyway = true;
        setText(gameCard);
    }

    @Override
    public void onBeginAnimation() {
        animationInProgress = true;
    }

    @Override
    public void onEndAnimation() {
        animationInProgress = false;
    }

    @Override
    public void update(CardView card) {
        this.updateCard = card;
        if (isPermanent && (card instanceof PermanentView)) {
            boolean needsTapping = isTapped() != ((PermanentView) card).isTapped();
            boolean needsFlipping = isFlipped() != ((PermanentView) card).isFlipped();
            if (needsTapping || needsFlipping) {
                Animation.tapCardToggle(this, this, needsTapping, needsFlipping);
            }
            if (needsTapping && ((PermanentView) card).isTapped()) {
                AudioManager.playTapPermanent();
            }
            boolean needsTranforming = isTransformed() != card.isTransformed();
            if (needsTranforming) {
                Animation.transformCard(this, this, card.isTransformed());
            }
        }
        if (card.canTransform()) {
            dayNightButton.setVisible(!isPermanent);
        }

        if (CardUtil.isCreature(card) && CardUtil.isPlaneswalker(card)) {
            ptText.setText(card.getPower() + "/" + card.getToughness() + " (" + card.getLoyalty() + ")");
        } else if (CardUtil.isCreature(card)) {
            ptText.setText(card.getPower() + "/" + card.getToughness());
        } else if (CardUtil.isPlaneswalker(card)) {
            ptText.setText(card.getLoyalty());
        } else {
            ptText.setText("");
        }
        setText(card);

        this.isPlayable = card.isPlayable();
        this.isChoosable = card.isChoosable();
        this.canAttack = card.isCanAttack();
        this.isSelected = card.isSelected();

        boolean updateImage = !gameCard.getName().equals(card.getName()) || gameCard.isFaceDown() != card.isFaceDown(); // update after e.g. turning a night/day card
        if (updateImage && gameCard.canTransform() && card.canTransform() && transformed) {
            if (card.getSecondCardFace() != null && card.getSecondCardFace().getName().equals(gameCard.getName())) {
                transformed = false;
            }
        }
        this.gameCard = card;

        String cardType = getType(card);
        tooltipText.setText(getText(cardType, card));

        if (hasSickness && CardUtil.isCreature(gameCard) && isPermanent) {
            overlayPanel.setVisible(true);
        } else {
            overlayPanel.setVisible(false);
        }
        if (updateImage) {
            updateImage();
            if (card.canTransform()) {
                BufferedImage transformIcon;
                if (transformed || card.isTransformed()) {
                    transformIcon = ImageManagerImpl.getInstance().getNightImage();
                } else {
                    transformIcon = ImageManagerImpl.getInstance().getDayImage();
                }
                dayNightButton.setIcon(new ImageIcon(transformIcon));
            }
        }

        if (counterPanel != null) {
            updateCounters(card);
        }

        repaint();
    }

    private void updateCounters(CardView card) {
        if (card.getCounters() != null && !card.getCounters().isEmpty()) {
            String name = "";
            if (lastCardWidth != cardWidth) {
                lastCardWidth = cardWidth;
                plusCounter = 0;
                minusCounter = 0;
                otherCounter = 0;
                loyaltyCounter = 0;
            }
            plusCounterLabel.setVisible(false);
            minusCounterLabel.setVisible(false);
            loyaltyCounterLabel.setVisible(false);
            otherCounterLabel.setVisible(false);
            for (CounterView counterView : card.getCounters()) {
                if (counterView.getCount() == 0) {
                    continue;
                }
                switch (counterView.getName()) {
                    case "+1/+1":
                        if (counterView.getCount() != plusCounter) {
                            plusCounter = counterView.getCount();
                            plusCounterLabel.setIcon(getCounterImageWithAmount(plusCounter, ImageManagerImpl.getInstance().getCounterImageGreen(), cardWidth));
                        }
                        plusCounterLabel.setVisible(true);
                        break;
                    case "-1/-1":
                        if (counterView.getCount() != minusCounter) {
                            minusCounter = counterView.getCount();
                            minusCounterLabel.setIcon(getCounterImageWithAmount(minusCounter, ImageManagerImpl.getInstance().getCounterImageRed(), cardWidth));
                        }
                        minusCounterLabel.setVisible(true);
                        break;
                    case "loyalty":
                        if (counterView.getCount() != loyaltyCounter) {
                            loyaltyCounter = counterView.getCount();
                            loyaltyCounterLabel.setIcon(getCounterImageWithAmount(loyaltyCounter, ImageManagerImpl.getInstance().getCounterImageViolet(), cardWidth));
                        }
                        loyaltyCounterLabel.setVisible(true);
                        break;
                    default:
                        if (name.isEmpty()) { // only first other counter is shown
                            name = counterView.getName();
                            otherCounter = counterView.getCount();
                            otherCounterLabel.setToolTipText(name);
                            otherCounterLabel.setIcon(getCounterImageWithAmount(otherCounter, ImageManagerImpl.getInstance().getCounterImageGrey(), cardWidth));
                            otherCounterLabel.setVisible(true);
                        }
                }
            }

            counterPanel.setVisible(true);
        } else {
            plusCounterLabel.setVisible(false);
            minusCounterLabel.setVisible(false);
            loyaltyCounterLabel.setVisible(false);
            otherCounterLabel.setVisible(false);
            counterPanel.setVisible(false);
        }

    }

    private static ImageIcon getCounterImageWithAmount(int amount, BufferedImage image, int cardWidth) {
        int factor = cardWidth > WIDTH_LIMIT ? 2 : 1;
        int xOffset = amount > 9 ? 2 : 5;
        int fontSize = factor == 1 ? amount < 10 ? 12 : amount < 100 ? 10 : amount < 1000 ? 7 : 6
                : amount < 10 ? 19 : amount < 100 ? 15 : amount < 1000 ? 12 : amount < 10000 ? 9 : 8;
        BufferedImage newImage;
        if (cardWidth > WIDTH_LIMIT) {
            newImage = ImageManagerImpl.deepCopy(image);
        } else {
            newImage = ImageHelper.getResizedImage(image, 20, 20);
        }
        Graphics graphics = newImage.getGraphics();
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Arial Black", amount > 100 ? Font.PLAIN : Font.BOLD, fontSize));
        graphics.drawString(Integer.toString(amount), xOffset * factor, 11 * factor);
        return new ImageIcon(newImage);
    }

    @Override
    public boolean contains(int x, int y) {
        return containsThis(x, y, true);
    }

    public boolean containsThis(int x, int y, boolean root) {
        Point component = getLocation();

        int cx = getCardX() - component.x;
        int cy = getCardY() - component.y;
        int cw = getCardWidth();
        int ch = getCardHeight();
        if (isTapped()) {
            cy = ch - cw + cx;
            ch = cw;
            cw = getCardHeight();
        }

        return x >= cx && x <= cx + cw && y >= cy && y <= cy + ch;
    }

    @Override
    public CardView getOriginal() {
        return this.gameCard;
    }

    @Override
    public Image getImage() {
        if (this.hasImage) {
            if (gameCard.isFaceDown()) {
                return getFaceDownImage();
            } else {
                return ImageCache.getImageOriginal(gameCard);
            }
        }
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (gameCard.hideInfo()) {
            return;
        }
        if (!tooltipShowing) {
            synchronized (this) {
                if (!tooltipShowing) {
                    TransferData transferData = getTransferDataForMouseEntered();
                    if (this.isShowing()) {
                        tooltipShowing = true;
                        callback.mouseEntered(e, transferData);
                    }
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        data.component = this;
        callback.mouseDragged(e, data);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (gameCard.hideInfo()) {
            return;
        }
        data.component = this;
        callback.mouseMoved(e, data);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (gameCard.hideInfo()) {
            return;
        }
        if (this.contains(e.getPoint())) {
            return;
        }
        if (tooltipShowing) {
            synchronized (this) {
                if (tooltipShowing) {
                    tooltipShowing = false;
                    data.component = this;
                    data.card = this.gameCard;
                    data.popupText = tooltipText;
                    callback.mouseExited(e, data);
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        data.component = this;
        data.card = this.gameCard;
        data.gameId = this.gameId;
        callback.mousePressed(e, data);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        callback.mouseReleased(e, data);
    }

    /**
     * Prepares data to be sent to action callback on client side.
     *
     * @return
     */
    private TransferData getTransferDataForMouseEntered() {
        data.component = this;
        data.card = this.gameCard;
        data.popupText = tooltipText;
        data.gameId = this.gameId;
        data.locationOnScreen = data.component.getLocationOnScreen(); // we need this for popup
        data.popupOffsetX = isTapped() ? cardHeight + cardXOffset + POPUP_X_GAP : cardWidth + cardXOffset + POPUP_X_GAP;
        data.popupOffsetY = 40;
        return data;
    }

    protected final String getType(CardView card) {
        StringBuilder sbType = new StringBuilder();

        for (String superType : card.getSuperTypes()) {
            sbType.append(superType).append(" ");
        }

        for (CardType cardType : card.getCardTypes()) {
            sbType.append(cardType.toString()).append(" ");
        }

        if (card.getSubTypes().size() > 0) {
            sbType.append("- ");
            for (String subType : card.getSubTypes()) {
                sbType.append(subType).append(" ");
            }
        }

        return sbType.toString().trim();
    }

    protected final String getText(String cardType, CardView card) {
        StringBuilder sb = new StringBuilder();
        if (card instanceof StackAbilityView || card instanceof AbilityView) {
            for (String rule : card.getRules()) {
                sb.append("\n").append(rule);
            }
        } else {
            sb.append(card.getName());
            if (card.getManaCost().size() > 0) {
                sb.append("\n").append(card.getManaCost());
            }
            sb.append("\n").append(cardType);
            if (card.getColor().hasColor()) {
                sb.append("\n").append(card.getColor().toString());
            }
            if (card.getCardTypes().contains(CardType.CREATURE)) {
                sb.append("\n").append(card.getPower()).append("/").append(card.getToughness());
            } else if (card.getCardTypes().contains(CardType.PLANESWALKER)) {
                sb.append("\n").append(card.getLoyalty());
            }
            if (card.getRules() == null) {
                card.overrideRules(new ArrayList<String>());
            }
            for (String rule : card.getRules()) {
                sb.append("\n").append(rule);
            }
            if (card.getExpansionSetCode() != null && card.getExpansionSetCode().length() > 0) {
                sb.append("\n").append(card.getCardNumber()).append(" - ");
                sb.append(card.getExpansionSetCode()).append(" - ");
                sb.append(card.getRarity().toString());
            }
        }
        return sb.toString();
    }

    @Override
    public void update(PermanentView card) {
        this.hasSickness = card.hasSummoningSickness();
        this.copyIconPanel.setVisible(card.isCopy());
        update((CardView) card);
    }

    @Override
    public PermanentView getOriginalPermanent() {
        if (isPermanent) {
            return (PermanentView) this.gameCard;
        }
        throw new IllegalStateException("Is not permanent.");
    }

    @Override
    public void updateCallback(ActionCallback callback, UUID gameId) {
        this.callback = callback;
        this.gameId = gameId;
    }

    public void setTransformed(boolean transformed) {
        this.transformed = transformed;
    }

    @Override
    public void toggleTransformed() {
        this.transformed = !this.transformed;
        if (transformed) {
            if (dayNightButton != null) { // if transformbable card is copied, button can be null
                BufferedImage night = ImageManagerImpl.getInstance().getNightImage();
                dayNightButton.setIcon(new ImageIcon(night));
            }
            if (this.gameCard.getSecondCardFace() == null) {
                LOGGER.error("no second side for card to transform!");
                return;
            }
            if (!isPermanent) { // use only for custom transformation (when pressing day-night button)
                this.temporary = this.gameCard;
                update(this.gameCard.getSecondCardFace());
            }
        } else {
            if (dayNightButton != null) { // if transformbable card is copied, button can be null
                BufferedImage day = ImageManagerImpl.getInstance().getDayImage();
                dayNightButton.setIcon(new ImageIcon(day));
            }
            if (!isPermanent) { // use only for custom transformation (when pressing day-night button)
                update(this.temporary);
                this.temporary = null;
            }
        }
        String temp = this.gameCard.getAlternateName();
        this.gameCard.setAlternateName(this.gameCard.getOriginalName());
        this.gameCard.setOriginalName(temp);
        updateImage();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (gameCard.hideInfo()) {
            return;
        }
        data.component = this;
        callback.mouseWheelMoved(e, data);
    }

    public JPanel getCardArea() {
        return cardArea;
    }

    @Override
    public void componentResized(ComponentEvent ce) {
        doLayout();
        // this update removes the isChoosable mark from targetCardsInLibrary
        // so only done for permanents because it's needed to redraw counters in different size, if window size was changed
        // no perfect solution yet (maybe also other not wanted effects for PermanentView objects)
        if (updateCard != null && (updateCard instanceof PermanentView)) {
            update(updateCard);
        }
    }

    @Override
    public void componentMoved(ComponentEvent ce) {
    }

    @Override
    public void componentShown(ComponentEvent ce) {
    }

    @Override
    public void componentHidden(ComponentEvent ce) {
    }

    @Override
    public void setTextOffset(int yOffset) {
        yTextOffset = yOffset;
    }

    @Override
    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    @Override
    public void setPopupMenu(JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }

}
