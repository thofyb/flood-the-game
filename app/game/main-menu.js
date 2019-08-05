import Phaser from 'phaser';

export default class MainMenu extends Phaser.Scene {
    constructor() {
        super({ key: 'MainMenu' })
    }

    preload() {
        this.load.image('logo', 'assets/games/main_menu/logo3.png')
        this.load.image('sp', 'assets/games/main_menu/sp.png')
        this.load.image('mp', 'assets/games/main_menu/mp.png')
        this.load.atlas('flood', 'assets/games/flood/blobs.png', 'assets/games/flood/blobs.json');
    }

    create() {
        this.add.image(400, 300, 'flood', 'background');

        this.logo = this.add.image(200 + 200, 90 + 40, 'logo').setScale(10, 10)

        this.spButton = this.add.image(400, 300 + 10, 'sp').setScale(2.5, 2.5)
        this.spButton.setInteractive();
        this.spButton.on('pointerup', () => {
            this.scene.stop('MainMenu');            
            this.scene.start('FloodSinglePlayer')
        })

        this.mpButton = this.add.image(400, 300 + 120, 'mp').setScale(2.5, 2.5);
        this.mpButton.setInteractive();
        this.mpButton.on('pointerup', () => {
            this.scene.stop('MainMenu');
            this.scene.start('FloodMultiPlayer')
        })
        
    }
}