import {
  Component,
  effect,
  inject,
  OnDestroy,
  signal,
  ViewChild,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../../../components/page-toolbar/page-toolbar.component";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatCardModule } from "@angular/material/card";
import { ConfigService } from "../../../../../services/config.service";
import { ApiResponseInterface } from "../../../../../domain/interfaces/api-response.interface";
import { MatButtonModule } from "@angular/material/button";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { SnackbarService } from "../../../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../../utils/enums/snackbar-type.enum";
import { MessageInterface } from "../../../../../domain/interfaces/config.interface";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { Subject, takeUntil } from "rxjs";
import { MessageUpdateDtoInterface } from "../../../../../domain/dto/config.dto";
import { MessageEditDialogComponent } from "../message-edit-dialog/message-edit-dialog.component";

@Component({
  selector: "app-messages-list",
  templateUrl: "./messages-list.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatCardModule,
    MatButtonModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatTooltipModule,
    MatDialogModule,
  ],
})
export class MessagesListComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  readonly dialog = inject(MatDialog);
  configService = inject(ConfigService);
  snackbarService = inject(SnackbarService);

  messages = signal<MessageInterface[]>([]);
  messagesFilter = new FormControl("");
  messagesDataSource: MatTableDataSource<MessageInterface> =
    new MatTableDataSource();
  messagesDisplayedColumns: string[] = [
    "id",
    "key",
    "value",
    "locale",
    "action",
  ];

  @ViewChild("messagesPaginator") messagesPaginator!: MatPaginator;
  @ViewChild("messagesSort") messagesSort!: MatSort;

  constructor() {
    this.loadInitialData();

    effect(() => {
      if (this.messages()) {
        this.messagesDataSource.data = this.messages();
        this.messagesDataSource.paginator = this.messagesPaginator;
        this.messagesDataSource.sort = this.messagesSort;
      }
    });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  loadInitialData() {
    this._getMessages();
    this._setupFilters();
  }

  editMessage(message: MessageInterface) {
    if (message != null) {
      const dialogRef = this.dialog.open(MessageEditDialogComponent, {
        data: { message: message },
      });
      dialogRef.afterClosed().subscribe((message: MessageInterface) => {
        if (message) {
          const messageDto: MessageUpdateDtoInterface = {
            id: message.id,
            key: message.key,
            locale: message.locale,
            value: message.value,
          };
          this.configService
            .updateMessage(messageDto)
            .pipe(takeUntil(this._destroy$))
            .subscribe((response: ApiResponseInterface<MessageInterface>) => {
              this.snackbarService.openSnackbar(
                response.message,
                3000,
                "center",
                "top",
                SnackbarTypeEnum.Success
              );
              this._getMessages();
            });
        }
      });
    } else
      this.snackbarService.openSnackbar(
        "Ocurrió un error el editar el elemento",
        3000,
        "center",
        "bottom",
        SnackbarTypeEnum.Error
      );
  }

  private _getMessages() {
    this.configService
      .getMessages()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<MessageInterface[]>) => {
        this.messages.set(response.data);
      });
  }

  private _setupFilters() {
    this.messagesFilter.valueChanges.subscribe((filterValue) => {
      this.messagesDataSource.filter = filterValue?.trim().toLowerCase()!;

      if (this.messagesDataSource.paginator) {
        this.messagesDataSource.paginator.firstPage();
      }
    });
  }
}
