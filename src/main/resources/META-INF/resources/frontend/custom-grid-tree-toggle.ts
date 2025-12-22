import { html, css } from 'lit';
import { customElement } from 'lit/decorators.js';
import { GridTreeToggle } from '@vaadin/grid/src/vaadin-grid-tree-toggle.js';

@customElement('custom-grid-tree-toggle')
export class CustomGridTreeToggle extends GridTreeToggle  {
	
  firstUpdated() {
	const parent = this.parentElement;
	const cell = parent?.getAttribute('slot')?.split('-')[4];
	const td = parent?.parentElement?.shadowRoot?.querySelector('#vaadin-grid-cell-'+cell);
    td?.addEventListener('keyup', (e : Event) => this._onKeyUp(e));
  }

  _onKeyUp(e : Event) {
	const keyEvent = e as KeyboardEvent;
	if (keyEvent.keyCode == 32) {
	  keyEvent.stopPropagation();
      this.dispatchEvent(new CustomEvent('click'));
    }
    if (keyEvent.keyCode == 13) {
      keyEvent.stopPropagation();
      this.dispatchEvent(new CustomEvent('doselect'));
    }
  }

}
