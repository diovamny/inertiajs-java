export namespace App {
    export namespace Models {
        export type User = {
            id: number;
            name: string;
            email: string;
            email_verified_at: string | null;
            created_at: string | null;
            updated_at: string | null;
            [key: string]: unknown;
        };

        export type Organization = {
            id: number;
            name: string;
            contacts_count?: number;
            created_at: string | null;
            updated_at: string | null;
            [key: string]: unknown;
        };

        export type Contact = {
            id: number;
            first_name: string;
            last_name: string;
            email: string | null;
            phone: string | null;
            organization_id?: number | null;
            organization?: Pick<Organization, 'id' | 'name'> | null;
            is_favorite: boolean;
            created_at: string | null;
            updated_at: string | null;
            [key: string]: unknown;
        };

        export type Note = {
            id: number;
            body: string;
            contact?: Contact | null;
            user?: User | null;
            created_at: string | null;
            updated_at: string | null;
            [key: string]: unknown;
        };
    }
}

export namespace Inertia {
    export namespace Pages {
        export namespace Contacts {
            export type Create = {
                organizations: App.Models.Organization[];
            };

            export type Edit = {
                contact: App.Models.Contact;
                organizations: App.Models.Organization[];
            };

            export type Index = {
                contacts: {
                    data: App.Models.Contact[];
                    next_cursor: string | null;
                    next_page_url: string | null;
                    prev_cursor: string | null;
                    prev_page_url: string | null;
                };
                filters: {
                    search: string;
                    favorite: boolean;
                };
            };

            export type Show = {
                contact: App.Models.Contact;
                notes?: App.Models.Note[];
            };
        }

        export namespace Crm {
            export type Dashboard = {
                totalContacts?: number;
                totalOrganizations?: number;
                recentNotesCount?: number;
                recentActivity: App.Models.Note[];
            };
        }

        export namespace Organizations {
            export type Index = {
                organizations: {
                    data: App.Models.Organization[];
                    meta: {
                        links: { url: string | null; label: string; active: boolean }[];
                    };
                };
                filters: {
                    search: string;
                };
            };

            export type Show = {
                organization: App.Models.Organization;
                contacts: {
                    data: App.Models.Contact[];
                    next_cursor: string | null;
                    next_page_url: string | null;
                    prev_cursor: string | null;
                    prev_page_url: string | null;
                };
            };
        }
    }
}
